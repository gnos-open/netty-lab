package org.gnos.netty.lab.server.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestBody {

    private int id;
    private String name;
    private LocalDateTime dateTime;

}
