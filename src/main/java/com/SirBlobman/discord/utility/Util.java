package com.SirBlobman.discord.utility;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Util {
    @SafeVarargs
    public static <L> List<L> newList(L... ll) {
        List<L> list = new ArrayList<L>();
        for(L l : ll) list.add(l);
        return list;
    }
    
    public static void log(String... ss) {
        PrintStream system = System.out;
        for(String message : ss) {
            if(message == null) continue;
            system.println(message);
        }
    }
    
    public static <L> List<L> newList(Collection<L> ll) {
        List<L> list = new ArrayList<>(ll);
        return list;
    }
    
    public static <K, V> Map<K, V> newMap() {
        return new HashMap<>();
    }
    
    public static <K, V> Map<K, V> newMap(Map<K, V> oldMap) {
        Map<K, V> map = new HashMap<>(oldMap);
        return map;
    }
    
    public static <K, V> Map<K, V> newMap(Collection<K> keyList, Collection<V> valueList) {
        Map<K, V> map = newMap();
        
        int keyListSize = keyList.size();
        int valueListSize = valueList.size();
        if(keyListSize != valueListSize) return map;
        
        Iterator<K> keyIterator = keyList.iterator();
        Iterator<V> valueIterator = valueList.iterator();
        while(keyIterator.hasNext() && valueIterator.hasNext()) {
            K key = keyIterator.next();
            V value = valueIterator.next();
            map.put(key, value);
        }
        
        return map;
    }
    
    public static String getFinalArgs(int start, String... args) {
        if(start < 0) return "";
        if(args.length == 0) return "";
        if(start >= args.length) return "";
        
        String[] array = Arrays.copyOfRange(args, start, args.length);
        return String.join(" ", array);
    }
    
    public static String getMultiLineCodeString(String original) {
        return "```\n" + original + "\n```"; 
    }
}