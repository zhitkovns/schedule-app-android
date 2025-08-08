package com.spbpu.schedule.RuzApi.models;

import com.spbpu.schedule.RuzApi.RuzSpbStu;

import org.json.simple.JSONObject;
//import com.spbpu.schedule.RuzApi.models.Room;
//import com.spbpu.schedule.RuzApi.models.Building;
import java.util.ArrayList;
import java.util.Objects;

public class Auditory {
    private final ArrayList<Room> rooms;
    private final Building building;

    public Auditory(ArrayList<Room> rooms, Building building) {
        this.rooms = rooms;
        this.building = building;
    }

    @Override
    public String toString() {
        return "Auditory{" +
                "rooms=" + rooms +
                ", building=" + building +
                '}';
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public Building getBuilding() {
        return building;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auditory auditory = (Auditory) o;
        return Objects.equals(getRooms(), auditory.getRooms()) && getBuilding().equals(auditory.getBuilding());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRooms(), getBuilding());
    }

    public static Auditory parseJSON(JSONObject jsonObject) {
        return new Auditory(
                com.spbpu.schedule.RuzApi.RuzSpbStu.getRooms((JSONObject) jsonObject.get("rooms")),
                com.spbpu.schedule.RuzApi.models.Building.parseJSON((JSONObject) jsonObject.get("building"))
        );
    }

}
