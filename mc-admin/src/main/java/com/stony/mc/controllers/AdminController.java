package com.stony.mc.controllers;

import com.stony.mc.dao.WorkerDao;
import com.stony.mc.manager.ServerInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/v1")
public class AdminController {


    @POST()
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map post() {
        Map map = new HashMap();
        map.put("id", 11111);
        map.put("name", "blue");
        return map;
    }

    @GET
    @Path("/get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map get() {
        Map map = new HashMap();
        map.put("id", 11111);
        map.put("name", "blue");
        return map;
    }

    WorkerDao dao = new WorkerDao();
    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServerInfo> list() {
        try {
            return dao.getWorkerInfoList(1, 10);
        } catch (SQLException e) {
           return new ArrayList<>(8);
        }
    }
}