package com.skydhs.czruby;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<String> ret = Arrays.asList("Hello,", "World", "from %country%");

        ret.stream().map(i -> i.replace("%country%", "USA")).collect(Collectors.toList()).forEach(System.out::println);
        ret.forEach(System.out::println);
    }
}