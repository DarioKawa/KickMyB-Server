package org.kickmyb.server.account;

import jakarta.persistence.*;
import org.kickmyb.server.task.MTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joris on 15-09-15.
 */

@Entity
public class MUser {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    public Long id;

    @Column(unique = true)
    public String username;

    @Basic
    public String password;

    // ORM style storage.
    @OneToMany(mappedBy = "user", fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MTask> tasks = new ArrayList<>();
}
