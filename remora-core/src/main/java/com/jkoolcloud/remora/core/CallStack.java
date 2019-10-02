package com.jkoolcloud.remora.core;

import java.util.Stack;

public class CallStack<T> extends Stack<EntryDefinition> {
    @Override
    public EntryDefinition push(EntryDefinition item) {
        System.out.println("Stack push: " + (this.size() +1));
        return super.push(item);
    }

    @Override
    public synchronized EntryDefinition pop() {
        System.out.println("Stack pop: " + (this.size()-1) );
        return super.pop();
    }
}
