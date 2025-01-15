#ifndef STACK_H
#define STACK_H

#include <stdbool.h>
#include "objRef.h"

typedef struct StackSlot_s{
    bool isObjRef; //TRUE --> objRef , FALSE --> number
    union{
        ObjRef objRef; //pointer on a object on the heap
        int number; //number, callback value, framepointer value
    }u;
    
}StackSlot;

//Method for pushing an object refrence onto the stack
void pushObjRef(ObjRef object);
//Method for popping an object refrence from the stack, returns that specific refrence
ObjRef popObjRef();

//these two methods are not the same as the ones that intialially come with the old stack, 
//because you also now have to specifiey that its about a number an not an Object (TRUE/FALSE)
void pushInteger(int number);
int popInteger();

#endif