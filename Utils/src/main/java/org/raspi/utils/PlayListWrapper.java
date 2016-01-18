/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.raspi.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vignesh
 */
public class PlayListWrapper {

    private final int index;
    private final List<File> playList = new ArrayList<>();

    public PlayListWrapper(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public List<File> getPlayList() {
        return playList;
    }    
}
