package com.github.cuteluobo.livedanmuarchive.service.database;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainDatabaseServiceTest {

    @Test
    void getListByFlag() {
        MainDatabaseService mainDatabaseService = MainDatabaseService.getInstance();
//        assertEquals(1,mainDatabaseService.getListByFlag(false, false, false, 1).size());
//        assertEquals(0,mainDatabaseService.getListByFlag(false, false, true, 1).size());
        System.out.println(mainDatabaseService.getListByFlag(false, false, false, 1).size());;
    }
}