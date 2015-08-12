package me.c_o_s.yandexmoneytreetest.model;

import java.util.List;

/**
 * Created by Constantine on 8/5/2015.
 */
public class Category {
    public long _id;
    public int id;
    public String title;
    public List<Category> subs;
}
