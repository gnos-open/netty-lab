package org.gnos.netty.lab.server.handler;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestModel {

    private int id;
    private String name;
    private long level;
    private int age;
    private LocalDateTime dateTime;

}
