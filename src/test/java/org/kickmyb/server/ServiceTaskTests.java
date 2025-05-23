package org.kickmyb.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kickmyb.server.account.MUser;
import org.kickmyb.server.account.MUserRepository;
import org.kickmyb.server.task.MTask;
import org.kickmyb.server.task.MTaskRepository;
import org.kickmyb.server.task.ServiceTask;
import org.kickmyb.transfer.AddTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.AccessDeniedException;
import java.util.Date;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO pour celui ci on aimerait pouvoir mocker l'utilisateur pour ne pas avoir à le créer

// https://reflectoring.io/spring-boot-mock/#:~:text=This%20is%20easily%20done%20by,our%20controller%20can%20use%20it.

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KickMyBServerApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@ActiveProfiles("test")
class ServiceTaskTests {

    @Autowired
    private MUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceTask serviceTask;

    @Autowired
    private MTaskRepository taskRepository;

    @Test
    void testAddTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);

        assertEquals(1, serviceTask.home(u.id).size());
    }

    @Test
    void testAddTaskEmpty()  {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Empty");
        } catch (Exception e) {
            assertEquals(ServiceTask.Empty.class, e.getClass());
        }
    }

    @Test
    void testAddTaskTooShort() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "o";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.TooShort");
        } catch (Exception e) {
            assertEquals(ServiceTask.TooShort.class, e.getClass());
        }
    }

    @Test
    void testAddTaskExisting() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }

    @Test
    void testDeleteTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing, AccessDeniedException {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);

        assertEquals(1, serviceTask.home(u.id).size());

        u = userRepository.findByUsername(u.username).get();

        long taskID = serviceTask.home(u.id).get(0).id;
        serviceTask.deleteOne(taskID, u);

        assertEquals(0, serviceTask.home(u.id).size());
    }

    @Test
    void testDeleteTaskNotFound() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing, AccessDeniedException {MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        long taskID = 1234567890L;

        try{
            serviceTask.deleteOne(taskID, u);
            fail("Aurait du lancer IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
        catch (Exception e) {
            fail("Aurait du lancer IllegalArgumentException");
        }
    }

    @Test
    void testDeleteNoAccess() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing, AccessDeniedException {
        MUser u = new MUser();
        u.username = "Alice";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        MUser u2 = new MUser();
        u2.username = "Bob";
        u2.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u2);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bob peut pas l'effacer";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(atr, u);

        long taskID = serviceTask.home(u.id).get(0).id;
        try{
            serviceTask.deleteOne(taskID, u2);
            fail("Aurait du lancer AccessDeniedException");
        } catch (AccessDeniedException e) {
            assertEquals(AccessDeniedException.class, e.getClass());
        }
        catch (Exception e) {
            fail("Aurait du lancer AccessDeniedException");
        }

    }

    @Test
    void testupdateTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing, AccessDeniedException {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(atr, u);

        long taskID = serviceTask.home(u.id).get(0).id;
        try {
            serviceTask.updateProgress(taskID, 50, u);
            MTask task = taskRepository.findById(taskID).get();
            assertEquals(50, task.events.get(0).resultPercentage );
        }
        catch (AccessDeniedException e) {
            fail("Aurait pas du lancer AccessDeniedException");
        }
    }

    @Test
    void testupdateTaskNoAcess() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing, AccessDeniedException {
        MUser u = new MUser();
        u.username = "Alice";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        MUser u2 = new MUser();
        u2.username = "Bob";
        u2.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u2);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bob peut pas le changer";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);
        long taskID = serviceTask.home(u.id).get(0).id;
        try{
            serviceTask.updateProgress(taskID, 50, u2);
            fail("Aurait du lancer AccessDeniedException");
        } catch (AccessDeniedException e) {
            assertEquals(AccessDeniedException.class, e.getClass());
        }
        catch (Exception e) {
            fail("Aurait du lancer AccessDeniedException");
        }
    }
}
