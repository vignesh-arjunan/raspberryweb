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

    private int index;
    private List<File> playList = new ArrayList<>();

    public int getIndex() {
        return index;
    }

    public List<File> getPlayList() {
        return playList;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPlayList(List<File> playList) {
        this.playList = playList;
    }

}
