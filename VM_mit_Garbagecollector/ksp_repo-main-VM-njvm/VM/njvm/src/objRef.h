#ifndef OBJREF_H
#define OBJREF_H
#include <stdint.h>


typedef struct ObjRef_s{
    void * forward_pointer;
    uint64_t size; //number of bytes of the payload
    bool brokenHeart;
    unsigned char data[1];
} *ObjRef;



//Method for creating a new object in the heap with the needed size, return a refrence to that object
ObjRef create_new_object(unsigned int size);

//Method for diplaying certain details about given object
void inspect_object(ObjRef object);

#endif