#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include "stack.h"
#include "objRef.h"
#include "bigint/build/include/bigint.h"
#include "bigint/build/include/support.h"


#define VERSION 7

#define HALT 0
#define PUSHC 1
#define ADD 2
#define SUB 3
#define MUL 4
#define DIV 5
#define MOD 6
#define RDINT 7
#define WRINT 8
#define RDCHR 9
#define WRCHR 10
#define PUSHG 11
#define POPG 12
#define ASF 13
#define RSF 14
#define PUSHL 15
#define POPL 16
#define EQ 17
#define NE 18
#define LT 19
#define LE 20
#define GT 21
#define GE 22
#define JMP 23
#define BRF 24
#define BRT 25
#define CALL 26
#define RET 27
#define DROP 28
#define PUSHR 29
#define POPR 30 
#define DUP 31
#define NEW 32
#define GETF 33
#define PUTF 34
#define NEWA 35
#define GETFA 36
#define PUTFA 37
#define GETSZ 38
#define PUSHN 39
#define REFEQ 40
#define REFNE 41

#define IMMEDIATE(x) ((x) & 0x00FFFFFF)
#define SIGN_EXTEND(i) ((i) & 0x00800000 ? (i) | 0xFF000000 : (i))

//#define DEBUG 

#define MAXITEMS 100000
#define MAXINSTRUCTS 30

#define MSB (1ULL << 63) // ex. 10000000000000000000000000000000, 0x80000000

#define IS_PRIMITIVE(objRef) (((objRef)->size & MSB) == 0)

#define GET_ELEMENT_COUNT(objRef) ((objRef)->size & ~MSB)

#define GET_REFS_PTR(objRef) ((ObjRef *) (objRef)->data)

//GC-Macros

//sets the broken heart flag for an object. The second highest Bit shows if this object already has been copied into the active memory
#define SET_BROKEN_HEART_FLAG(objRef) ((objRef)->size = objRef->size | (1ULL << 62))

//checks if the broken heart flag has been set for an given object, if the flag has been set that means that the object has already been copied and the forward pointer is valid
#define IS_BROKEN_HEART_FLAG_SET(objRef) (((objRef)->size & (1ULL << 62)) != 0)

//sets the forward pointer of an object, that pointer points to the free memory in the active memory
#define SET_FORWARD_POINTER(objRef, pointer) ((objRef)->size = (uint64_t)(pointer)) 

//return the forward pointer of a given object
#define GET_FORWARD_POINTER(objRef) ((uint64_t)((objRef)->size & (~((1ULL << 62) | MSB)))) 


int pc=0;
int sp=0;
int fp=0;
int num_of_glob_vars;
ObjRef *sda = NULL; 
StackSlot *stack;

//int stack_length = sizeof(stack) / sizeof(stack[0]);
size_t stack_length;
size_t stack_length_new;

//variable for determening if the debug mode is activated or not
int debug_mode;
int gc_flag;
//initialize with NULL, so that there is no chance of some random old values to still be in this register
ObjRef ret_value_reg = NULL;

char *heap = NULL;
int heap_size_global = 0;
char *active_memory;
char *passive_memory;
char *free_pointer;
char *end_of_active_memory;

//Memory Management
void make_stack(int stack_size){

    
        stack_length_new = (stack_size * 1024) / sizeof(StackSlot); //important for checking for stack overflow 
        stack = malloc(stack_length_new * sizeof(StackSlot));
        if(stack == NULL){
            fprintf(stderr,"Memory for stack couldnt be allocated!\n");
            exit(1);
        }

        stack_length = stack_length_new;  
        // printf("Stack initialized with %zu slots (%d KiB).\n", stack_length, stack_size);
}

void make_heap(int heap_size){
    heap_size_global = heap_size * 1024;
    heap = calloc(1,heap_size_global); 
    if(heap == NULL){
        fprintf(stderr,"Memory for heap couldnt be allocated!\n");
            exit(1);
    }

    //heap split 
    active_memory = heap; // first half of the heap
    passive_memory = heap + (heap_size_global / 2); //IF "/ 1" THEN HEAP HAS MORE MEMORY      SUMBMISSION: 17/27 (62%)
    free_pointer = active_memory;
    end_of_active_memory = passive_memory;

    // printf("Heap initialized with %d KiB.\n", heap_size);

     //printf("Active memory starts at: %p, Passive memory starts at: %p\n", 
        //   (void *)active_memory, (void *)passive_memory);
}

void garbageCollector();

void *myMalloc(size_t size){

    //checking if there is enough memory in the active memory half
    if(((free_pointer + size) > end_of_active_memory)){
       // fprintf(stderr,"Heap has no more memory in this half!\n");
            //return NULL;


        garbageCollector();

        //checking if there is enough memory after the GC-run
        if(((free_pointer + size) > end_of_active_memory)){
            // fprintf(stderr,"EVEN AFTER GC NOT ENOUGH MEMORY!\n");
            exit(1);
        }
    }

    char *free_memory_area = free_pointer;
    free_pointer = free_pointer + size;

    return free_memory_area;

}


//GC-Methods


ObjRef copyObjectToFreeMem(ObjRef orig){

    
    size_t object_size;
    //initial size of every object primitive or compund
    size_t object_size_universal = sizeof(struct ObjRef_s);

    //calculating the objects size based on its type
    if(!IS_PRIMITIVE(orig)){
        //COMPUND OBJECT
        size_t compund = GET_ELEMENT_COUNT(orig) * sizeof(void *);
        object_size = object_size_universal + compund;
    }else{
        //PRIMITIVE OBJECTS
        size_t primitive = GET_ELEMENT_COUNT(orig);
        object_size = object_size_universal + primitive;
    }


    char* copy = free_pointer;
    free_pointer += object_size;


    return  (ObjRef)memcpy(copy, orig, object_size);
}


ObjRef relocate(ObjRef orig){
    ObjRef copy;
    
    //if the object is NULL then the copy will also be NULL
    if(orig == NULL){
        copy = NULL;
         
    }else{

        if(orig->brokenHeart){
         copy = (ObjRef)orig->forward_pointer;
        }else{
            copy = copyObjectToFreeMem(orig);
            orig->brokenHeart = true;
            orig->forward_pointer = copy;
        }

    }

    return copy;
}


void getRootObjects(){


    // BIP registers
    
        bip.op1 = relocate(bip.op1);
    

        // printf("op1!\n");

     
        bip.op2 = relocate(bip.op2);
    

         //printf("op2!\n");

    
        bip.res = relocate(bip.res);
    

        // printf("res\n");

     
        bip.rem = relocate(bip.rem);
    

      //  printf("rem\n");

    //SDA
    for(int i = 0; i<num_of_glob_vars;i++){
        
         sda[i] = relocate(sda[i]);
        
    }

    //  printf("sda\n");

    //StackSlots
    for(int i= 0; i< sp; i++){
        if(stack[i].isObjRef){
            stack[i].u.objRef = relocate(stack[i].u.objRef);
        }
    }

    //  printf("stack\n");

    //RVR
    
        ret_value_reg = relocate(ret_value_reg);
    

    //  printf("rvr\n");

   // printf("all root objects collected!\n");


}


void scan(){

    //points to the beginning of the active half of the heap memory
    char *scan = active_memory;


    //as long as the scan pointer finds valid refrences, the loop goes on
    while(scan < free_pointer){

        ObjRef object = (ObjRef) scan;


        //checking if the object is a compund object
        if(!IS_PRIMITIVE(object)){
            
        
            //number of elements which are inside that compound object
            size_t num_of_elements = GET_ELEMENT_COUNT(object);

            //relocating every refrence that is still valid
            for(int i = 0; i < num_of_elements; i++){                    //int or size_t for index ?
                
                 GET_REFS_PTR(object)[i] = relocate( GET_REFS_PTR(object)[i]); 
                
            }
        }


        size_t object_size_universal = sizeof(struct ObjRef_s);
        
        if(!IS_PRIMITIVE(object)){
            size_t compund = GET_ELEMENT_COUNT(object) * sizeof(ObjRef);
            scan = scan + object_size_universal + compund;

        }else{

            size_t primitive = GET_ELEMENT_COUNT(object);
            scan = scan + object_size_universal + primitive;
        }

    }


}


void garbageCollector(){

   // printf("GC CALLED!\n");

    //switching the passive memory half and the active to the passive

    char* switchup = active_memory;
    active_memory = passive_memory;
    passive_memory = switchup;
    free_pointer = active_memory;
    end_of_active_memory = active_memory + (heap_size_global / 2);

    //getting the root objects
    getRootObjects();

    //scanning so no refrences are lost
    scan();

    if(1){
        memset(passive_memory, 0, heap_size_global / 2);
    }



}




ObjRef newCompundObject(int numObjRefs){ 

    ObjRef object = (ObjRef)myMalloc(sizeof(struct ObjRef_s) + numObjRefs * (sizeof(void *)));
    if(object == NULL){
        printf("Error: heap overflow\n");
        exit(1);
    }

    //mark the Object as a comp. obj by setting the MSB to 1
    object->size =  MSB | numObjRefs ;

    //initializeing all refrences of  the comp. obj with nil

    for(int i = 0; i < GET_ELEMENT_COUNT(object); i++){
       GET_REFS_PTR(object)[i] = NULL;
    }

    return object;
}


void * newPrimObject(int dataSize) {
  ObjRef objRef;

  objRef = (ObjRef)myMalloc(sizeof(struct ObjRef_s) + dataSize );
  if (objRef == NULL) {
    printf("Error: heap overflow\n");
    exit(1);
  }
  objRef->size = dataSize;
  return objRef;
}

void fatalError(char *msg) {
  printf("FATAL ERROR: %s\n", msg);
  exit(1);
}


void * getPrimObjectDataPointer(void * obj){
    ObjRef oo = ((ObjRef)  (obj));
    return oo->data;
}

void reset_bips(){
    bip.op1 = NULL;
    bip.op2 = NULL;
    bip.res = NULL;
    bip.rem = NULL;
}



//Method for diplaying certain details about given object
void inspect_object(ObjRef object){

    if(object == NULL){
       fprintf(stderr, "Object is NULL!\n");
       return; 
    }
    printf("Object : %p | Size: %llu | Data: ", (void *)object, (unsigned long long)object->size);

    for(int i = 0; i < object->size;i++){
        printf("%02x", object->data[i]);
    }
    printf("\n");
}



//Method for pushing an object refrence onto the stack
void pushObjRef(ObjRef object){


    if(sp == MAXITEMS - 1){
        printf("STACK IS FULL !!!\n");
        exit(1);
    }
    

    // Alternative: StackSlot sl; sl.isObjRef = true; sl.u.objRef = object; Abfangen ob NULL drinsteht!! 

    stack[sp].isObjRef = true;
    stack[sp].u.objRef = object;
    sp++;
}


//Method for popping an object refrence from the stack, returns that specific refrence
ObjRef popObjRef(){

     if(sp == 0){
        printf("STACK IS EMPTY !!!\n");
       exit(1);
    }

    if(!stack[sp - 1].isObjRef){
        printf("Value is not an object refrence!\n");
        exit(1);
    }
    sp--;
    ObjRef tmp = stack[sp].u.objRef;
    return tmp;
}




void debug_stack() {
    printf("\nDEBUG: Stack (sp = %d, fp = %d):\n",sp, fp);
    printf("  +------+-------------------+-------------------------+\n");
    printf("  |  SP  |     Type          |         Value           |\n");
    printf("  +------+-------------------+-------------------------+\n");

    for (int i = sp - 1; i >= 0; i--) {
        if (stack[i].isObjRef) {
            // Wenn der Stack-Eintrag eine Objektreferenz ist
            ObjRef obj = stack[i].u.objRef;
            printf("  | %4d | ObjRef            | Address: %p, Size: %llu |\n", 
                   i, (void *)obj, (unsigned long long)obj->size);
        } else {
            // Wenn der Stack-Eintrag eine einfache Zahl ist
            printf("  | %4d | Integer/Address   | %d                     |\n", 
                   i, stack[i].u.number);
        }
    }

    printf("  +------+-------------------+-------------------------+\n\n");
}







void pushInteger(int number){

    if(sp == MAXITEMS - 1){
        printf("STACK IS FULL !!!\n");
        exit(1);
    }

    stack[sp].isObjRef = false;
    stack[sp].u.number = number;
    sp++;
}


int popInteger(){

     if(sp == 0){
        printf("STACK IS EMPTY !!!\n");
        exit(1);
    }

    if(stack[sp - 1].isObjRef){
        printf("Value is not a number!\n");
        exit(1);
    }

    sp--;
    int tmp = stack[sp].u.number;
    return tmp;
}








unsigned int *program_memory;
int program_memory_size = 0;

//Method for executing each instruction found in the program
void execute(unsigned int instruction) {
    // Extract opcode from bytecode
    int opcode = instruction >> 24;
    // Extract the correct immediate 
    int immediate = SIGN_EXTEND(instruction & 0x00FFFFFF);

    switch (opcode) {
        case HALT: // Ending the program
            return;

        case PUSHC:{ // Pushing a value onto the stack
        #ifdef DEBUG
        printf("PUSHC CALLED!\n");
        #endif
            reset_bips();
            bigFromInt(immediate);
            pushObjRef(bip.res);
            reset_bips();

            
            break;
        }
        case ADD:{ // Adding two values from the stack
        #ifdef DEBUG
        printf("ADD CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            bigAdd();
            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case SUB: { // Subtracting two values from the stack
        #ifdef DEBUG
        printf("SUB CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            bigSub();
            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case MUL: { // Multiplying two values from the stack+
        #ifdef DEBUG
        printf("MUL CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            bigMul();
            pushObjRef(bip.res);
            reset_bips();
        
            break;
        }

        case DIV: { // Dividing two values from the stack
        #ifdef DEBUG
        printf("DIV CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            bigDiv();
            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case MOD: { // Modulating two values from the stack
        #ifdef DEBUG
        printf("MOD CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            bigDiv();
            pushObjRef(bip.rem);
            reset_bips();
            break;
        }

        case RDINT: { // Reading an integer from user
        #ifdef DEBUG
        printf("RDINT CALLED!\n");
        #endif
           reset_bips();
           bigRead(stdin);
           pushObjRef(bip.res);
           reset_bips();

            break;
        }

        case WRINT: { // Writing the most recent value as integer
        #ifdef DEBUG
        printf("WRINT CALLED!\n");
        #endif
        
            reset_bips();
            bip.op1 = popObjRef();
            bigPrint(stdout);           
            reset_bips();
            break;
        }

        case RDCHR: { // Reading a character from user
        #ifdef DEBUG
        printf("RDCHR CALLED!\n");
        #endif
          
            reset_bips();
            char user_input;
            scanf(" %c", &user_input);
            bigFromInt((int)user_input); //converting the read input to a bigInt
            pushObjRef(bip.res);
            reset_bips();

            break;
        }

        case WRCHR: { // Writing the most recent value as character
        #ifdef DEBUG
        printf("WRCHR CALLED!\n");
        #endif
           
            reset_bips();
            bip.op1 = popObjRef();
            //int char_value = bigToInt();
            printf("%c", bigToInt());
            reset_bips(); 
           
            break;
        }

        case PUSHG: { // Push static data area value to stack
        #ifdef DEBUG
        printf("PUSHG CALLED!\n");
        #endif
           
             if (immediate < num_of_glob_vars && immediate >= 0) {
                ObjRef object = sda[immediate];
                pushObjRef(object);
            } else {
                fprintf(stderr, "Given index for PUSHG is not valid!\n");
                exit(1);
            }

            break;
        }

        case POPG: { // Pop stack value to static data area
        #ifdef DEBUG
        printf("POPG CALLED!\n");
        #endif
            if (immediate < num_of_glob_vars && immediate >= 0) {
                ObjRef object = popObjRef();
                sda[immediate] = object;
            } else {
                fprintf(stderr, "Given index for POPG is not valid!\n");
                exit(1);
            }

           
            break;
        }

        case ASF: // Allocate stack frame
        #ifdef DEBUG
        printf("ASF CALLED!\n");
        #endif
            pushInteger(fp);
            fp = sp;

            // initializing the allocated slots with nil and marking them as object refrences
            for(int i = 0; i < immediate; i++){
                stack[sp + i].isObjRef = true;
                stack[sp + i].u.objRef = NULL;
            }
            sp = sp + immediate;
            break;

        case RSF: // Remove stack frame
        #ifdef DEBUG
        printf("RSF CALLED!\n");
        #endif
            sp = fp;
            fp = popInteger();
            break;

        case PUSHL: // Push local variable onto stack
        #ifdef DEBUG
        printf("PUSHL CALLED!\n");
        #endif
            if(!stack[fp + immediate].isObjRef){
                printf("Value is not an object refrence!\n");
            }



            pushObjRef(stack[fp + immediate].u.objRef);
            break;

        case POPL: { // Pop value to local variable
        #ifdef DEBUG
        printf("POPL CALLED!\n");
        #endif
            ObjRef object = popObjRef();
            stack[fp + immediate].isObjRef = true;
            stack[fp + immediate].u.objRef = object;

            break;
        }

        case EQ: { // Equal comparison
        #ifdef DEBUG
        printf("EQ CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();
            
            int b;
            
            
            
            if(result == 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
           

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case NE: { // Not equal comparison
        #ifdef DEBUG
        printf("NE CALLED!\n");
        #endif
           reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();
            int b;
            
           
            
            if(result != 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
           

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case LT: { // Less than comparison
        #ifdef DEBUG
        printf("LT CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();
            int b;
            
           
            
            if(result < 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
            

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case LE: { // Less than or equal comparison
        #ifdef DEBUG
        printf("LE CALLED!\n");
        #endif
            reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();
            int b;
            
           
            
            if(result <= 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
            

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case GT: { // Greater than comparison
        #ifdef DEBUG
        printf("GT CALLED!\n");
        #endif
           reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();

            int b;
            
           
            
            if(result > 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
           

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case GE: { // Greater than or equal comparison
        #ifdef DEBUG
        printf("GE CALLED!\n");
        #endif
           reset_bips();
            bip.op2 = popObjRef();
            bip.op1 = popObjRef();
            int result = bigCmp();
            int b;
            
            
            
            if(result >= 0){
                //*(int *)result_obj->data = 1;
                b = 1;
            }else{
                //*(int *)result_obj->data = 0;
                b = 0;
            }

            bigFromInt(b);
            

            pushObjRef(bip.res);
            reset_bips();
            break;
        }

        case JMP: // Jump to address
        #ifdef DEBUG
        printf("JMP CALLED!\n");
        #endif
            pc = immediate;
            break;

        case BRF: { // Branch if false
        #ifdef DEBUG
        printf("BRF CALLED!\n");
        #endif
           
            bip.op1 = popObjRef();
            if(bigToInt() == 0){
                pc = immediate;
            }

            break;
        }

        case BRT: { // Branch if true
        #ifdef DEBUG
        printf("BRT CALLED!\n");
        #endif
                
            bip.op1 = popObjRef();
            if(bigToInt() == 1){
                pc = immediate;
            }
            break;
        }

        case CALL: // Call subroutine
        #ifdef DEBUG
        printf("CALL CALLED!\n");
        #endif
            pushInteger(pc);
            pc = immediate;
            break;

        case RET: { // Return from subroutine
        #ifdef DEBUG
        printf("RET CALLED!\n");
        #endif
            int ret_address = popInteger();
            pc = ret_address;
            break;
        }

        case DROP: // Drop n elements from stack
        #ifdef DEBUG
        printf("DROP CALLED!\n");
        #endif
            for (int i = 0; i < immediate; i++) {
                popObjRef();
            }
            break;

        case PUSHR: // Push return register value
        #ifdef DEBUG
        printf("PUSHR CALLED!\n");
        #endif
            // checking if the RVR is nil, so we dont push some nil value onto the stack
            if(ret_value_reg == NULL){
                fprintf(stderr, "RVR is currently nil, thus cannot be pushed!\n");
                exit(1);
            }
            pushObjRef(ret_value_reg);
            break;

        case POPR: { // Pop to return register
        #ifdef DEBUG
        printf("POPR CALLED!\n");
        #endif
            ObjRef object = popObjRef();
            ret_value_reg = object;
            break;
        }

        case DUP: { // Duplicate top of stack
        #ifdef DEBUG
        printf("DUP CALLED!\n");
        #endif
            ObjRef object = popObjRef();
            pushObjRef(object);
            pushObjRef(object);
            break;
        } 

        case NEW: { //creating a new record with n (immediate) elements 
        #ifdef DEBUG
        printf("NEW CALLED!\n");
        #endif
            int size = immediate;
        
            ObjRef record = newCompundObject(size);
            pushObjRef(record); 
           
            break;
        }

         case GETF: { // gets field from array and pushes it onto the stack
         #ifdef DEBUG
        printf("GETF CALLED!\n");
        #endif
            ObjRef object = popObjRef();

            if(object == NULL){
                fprintf(stderr,"Could not GETF because refrence is NULL!\n");
                exit(1);
            }

            if(IS_PRIMITIVE(object)){
                fprintf(stderr,"Could not GETF because object is primitve!\n");
                exit(1);
            }

            if(immediate < 0 || immediate > GET_ELEMENT_COUNT(object)){
                fprintf(stderr,"Could not GETF because index is out of bound!\n");
                exit(1);
            }

            pushObjRef(GET_REFS_PTR(object)[immediate]);
            break;
        }

        case PUTF: {  // sets the value of an field into a compund object
        #ifdef DEBUG
        printf("PUTF CALLED!\n");
        #endif
       

            ObjRef value_to_be_put = popObjRef();
            ObjRef object = popObjRef();


            if(object == NULL){
                fprintf(stderr,"Could not PUTF because refrence is NULL!\n");
                exit(1);
            }

            if(IS_PRIMITIVE(object)){
                fprintf(stderr,"Could not PUTF because object is primitve!\n");
                exit(1);
            }

            if(immediate < 0 || immediate > GET_ELEMENT_COUNT(object)){
                fprintf(stderr,"Could not PUTF because index is out of bound!\n");
                exit(1);
            }

            GET_REFS_PTR(object)[immediate] = value_to_be_put;
            
            break;
        }

        case NEWA:{
            #ifdef DEBUG
        printf("NEWA CALLED!\n");
        #endif
            reset_bips();
        
            bip.op1 = popObjRef(); 
            int arraySize = bigToInt();  // is BIG TO INT really neccesary ??!
            
            if(arraySize < 0){
                fprintf(stderr,"Index out of bound!\n");
                exit(1);
            }
            ObjRef array = newCompundObject(arraySize);

            pushObjRef(array);
            
            reset_bips();
            break;
        }

        case GETFA:{
            #ifdef DEBUG
        printf("GETFA CALLED!\n");
        #endif
            reset_bips();

            bip.op1 = popObjRef(); 
            int index = bigToInt();

             ObjRef array = popObjRef();

            if(array == NULL){
                fprintf(stderr,"Could not GETFA because refrence is NULL!\n");
                exit(1);
            }

            if(IS_PRIMITIVE(array)){
                fprintf(stderr,"Could not GETFA because object is primitve!\n");
                exit(1);
            }

            if(index < 0 || index > GET_ELEMENT_COUNT(array)){
                fprintf(stderr,"Could not GETFA because index is out of bound!\n");
                exit(1);
            }

            pushObjRef(GET_REFS_PTR(array)[index]);

            reset_bips();
            break;
        }

        case PUTFA:{
            #ifdef DEBUG
        printf("PUTFA CALLED!\n");
        #endif
    
             reset_bips();

            ObjRef value_to_be_put = popObjRef();

                bip.op1 = popObjRef(); 
                int index = bigToInt();

            

            ObjRef array = popObjRef();
            

             

            if(array == NULL){
                fprintf(stderr,"Could not PUTFA because refrence is NULL!\n");
                exit(1);
            }

            if(IS_PRIMITIVE(array)){
                fprintf(stderr,"Could not PUTFA because object is primitve!\n");
                exit(1);
            }


            
            if(index < 0 || index >= GET_ELEMENT_COUNT(array)){
                fprintf(stderr, "Could not PUTFA because index is out of bound! Index: %d, Array Size: %llu\n",
                index, (unsigned long long)GET_ELEMENT_COUNT(array));
                exit(1);
            }

            GET_REFS_PTR(array)[index] = value_to_be_put;

            reset_bips();


            //inspect_array(array);
            break;
        }

        case GETSZ:{
            #ifdef DEBUG
        printf("GETSZ CALLED!\n");
        #endif
            ObjRef object = popObjRef();

            if(object == NULL){
                fprintf(stderr,"Could not GETSZ because refrence is NULL!\n");
                exit(1);
            }

            if(IS_PRIMITIVE(object)){
                
            
                bigFromInt(-1);

                
                pushObjRef(bip.res);
                
            }else{

                
                
                bigFromInt(GET_ELEMENT_COUNT(object));

               
                pushObjRef(bip.res);
            }

            break;
        }

        case PUSHN:{
            #ifdef DEBUG
        printf("PUSHN CALLED!\n");
        #endif
             pushObjRef(NULL);

            break;
        }

        case REFEQ:{
            #ifdef DEBUG
        printf("REFEQ CALLED!\n");
        #endif
            ObjRef ref1 = popObjRef();
            ObjRef ref2 = popObjRef();

            if(ref1 == ref2){
                 
            
                bigFromInt(1);

                
                pushObjRef(bip.res);
            }else{
                
            
                bigFromInt(0);

               
                pushObjRef(bip.res);
            }

            break;
        }

         case REFNE:{
            #ifdef DEBUG
        printf("REFNE CALLED!\n");
        #endif
            ObjRef ref1 = popObjRef();
            ObjRef ref2 = popObjRef();

            if(ref1 != ref2){
                 
            
                bigFromInt(1);

               
                pushObjRef(bip.res);
            }else{
               
            
                bigFromInt(0);

                
                pushObjRef(bip.res);
            }

            break;
        }

        default: // Unknown opcode
            fprintf(stderr, "Unknown opcode: %d\n", opcode);
            exit(1);
    }
}




//Method for printing the program thats currently in die program memory
void print_program(){
//int program_length = sizeof(programm_memory) / sizeof(programm_memory[0]);

for(int i=0;i<program_memory_size;i++){
//extract opcode from bytecode
int opcode = program_memory[i] >> 24;
//extract the correct immediate
int immediate = SIGN_EXTEND(program_memory[i] & 0x00FFFFFF);

printf("%03d:    ", i);

switch (opcode) {
            case HALT:
                printf("halt\n");
                break;
            case PUSHC:
                printf("pushc    %d\n", immediate);
                break;
            case ADD:
                printf("add\n");
                break;
            case SUB:
                printf("sub\n");
                break;
            case MUL:
                printf("mul\n");
                break;
            case DIV:
                printf("div\n");
                break;
            case MOD:
                printf("mod\n");
                break;
            case RDINT:
                printf("rdint\n");
                break;
            case WRINT:
                printf("wrint\n");
                break;
            case RDCHR:
                printf("rdchr\n");
                break;
            case WRCHR:
                printf("wrchr\n");
                break;
            case PUSHG:
                printf("pushg    %d\n", immediate);
                break;
            case POPG:
                printf("popg     %d\n", immediate);
                break;
            case ASF:
                printf("asf      %d\n", immediate);
                break;
            case RSF:
                printf("rsf\n");
                break;
            case PUSHL:
                printf("pushl    %d\n", immediate);
                break;
            case POPL:
                printf("popl     %d\n", immediate);
                break;
            case EQ:
                printf("eq\n");
                break;
            case NE:
                printf("ne\n");
                break;
            case LT:
                printf("lt\n");
                break;
            case LE:
                printf("le\n");
                break;
            case GT:
                printf("gt\n");
                break;
            case GE:
                printf("ge\n");
                break;
            case JMP:
                printf("jmp      %d\n", immediate);
                break;
            case BRF:
                printf("brf      %d\n", immediate);
                break;
            case BRT:
                printf("brt      %d\n", immediate);
                break;
            case CALL:
                printf("call     %d\n", immediate);
                break;
            case RET:
                printf("ret\n");
                break;
            case DROP:
               printf("drop     %d\n", immediate);
                break;
            case PUSHR:
                printf("pushr\n");
                break;
            case POPR:
                printf("popr\n");
                break;
            case DUP:
                printf("dup\n");
                break;
            default:
                printf("unknown  %d\n", opcode);
                break;
        }
}




}


//Method for printing a single instruction
void print_instruction(unsigned int instruction, int pc) {
    // Extract the correct immediate
    int immediate = SIGN_EXTEND(instruction & 0x00FFFFFF);
    // Extract opcode from bytecode
    int opcode = instruction >> 24;

    switch (opcode) {
        case HALT:
            printf("%03d:     halt\n", pc);
            break;

        case PUSHC:
            printf("%03d:     pushc     %d\n", pc, immediate);
            break;

        case ADD:
            printf("%03d:     add\n", pc);
            break;

        case SUB:
            printf("%03d:     sub\n", pc);
            break;

        case MUL:
            printf("%03d:     mul\n", pc);
            break;

        case DIV:
            printf("%03d:     div\n", pc);
            break;

        case MOD:
            printf("%03d:     mod\n", pc);
            break;

        case RDINT:
            printf("%03d:     rdint\n", pc);
            break;

        case WRINT:
            printf("%03d:     wrint\n", pc);
            break;

        case RDCHR:
            printf("%03d:     rdchr\n", pc);
            break;

        case WRCHR:
            printf("%03d:     wrchr\n", pc);
            break;

        case PUSHG:
            printf("%03d:     pushg     %d\n", pc, immediate);
            break;

        case POPG:
            printf("%03d:     popg      %d\n", pc, immediate);
            break;

        case ASF:
            printf("%03d:     asf       %d\n", pc, immediate);
            break;

        case RSF:
            printf("%03d:     rsf\n", pc);
            break;

        case PUSHL:
            printf("%03d:     pushl     %d\n", pc, immediate);
            break;

        case POPL:
            printf("%03d:     popl      %d\n", pc, immediate);
            break;

        case EQ:
            printf("%03d:     eq\n", pc);
            break;

        case NE:
            printf("%03d:     ne\n", pc);
            break;

        case LT:
            printf("%03d:     lt\n", pc);
            break;

        case LE:
            printf("%03d:     le\n", pc);
            break;

        case GT:
            printf("%03d:     gt\n", pc);
            break;

        case GE:
            printf("%03d:     ge\n", pc);
            break;

        case JMP:
            printf("%03d:     jmp       %d\n", pc, immediate);
            break;

        case BRF:
            printf("%03d:     brf       %d\n", pc, immediate);
            break;

        case BRT:
            printf("%03d:     brt      %d\n", pc, immediate);
            break;

        case CALL:
            printf("%03d:     call      %d\n", pc, immediate);
            break;

        case RET:
            printf("%03d:     ret\n", pc);
            break;

        case DROP:
            printf("%03d:     drop      %d\n", pc, immediate);
            break;

        case PUSHR:
            printf("%03d:     pushr\n", pc);
            break;

        case POPR:
            printf("%03d:     popr\n", pc);
            break;

        case DUP:
            printf("%03d:     dup\n", pc);
            break;

        default:
            fprintf(stderr, "Unknown opcode: %d\n", opcode);
            exit(1);
    }
}





//method which controls the program counter and program memory (DEPRECATED)
void start_program(){
    //initialize opcode with something that does not exist
    int opcode = 666;
    unsigned int instruction;
    pc = 0;
    while (opcode != HALT){ //looping until exact contidion like HALT is met

        instruction = program_memory[pc];
        pc++;
        execute(instruction);
        opcode = instruction >> 24;
        //print_stack_new(); //option to print the stack after every instruction
        
    }
    
}

//telling the compiler that theres going to be a method called 'debugger'
void debugger();

//Method for controling the program, if the debug mode is activated the debugger takes the whole control of the program, otherwise the program just runs normally
void start_program_new(){

    while(1){
        if(debug_mode == 1){
            debugger();
            return;
        }

        unsigned int instruction = program_memory[pc];
        pc++;
        execute(instruction);

        int opcode = instruction >> 24;
        if(opcode == HALT){
            return;
        }
    }
}


//Method for reading the important specifiers from a ninja binary file and executing it by loading the program into the program memory
void execute_binary(char *filename){
    //open the file to be read and ensuring that it can be opend
    FILE *file_pointer = NULL;
    if((file_pointer = fopen(filename, "r"))==NULL){
        fprintf(stderr,"Error: cannot open code file '%s'\n", filename);
        return;
    }
    
    //reading the first 4 Bytes of the file and ensuring that its an ninja binary
    char format[4]; //making enough room for 4 Chars
    fread(format, 1,4,file_pointer); //reading the first 4 Bytes from the file into the format arry
    if(strncmp(format, "NJBF", 4) != 0){ //checking if the array matches the correct pattern
        fprintf(stderr,"Error: file '%s' is not a Ninja binary\n", filename);
        fclose(file_pointer);
        return;
    }
    
    //reading the files version number and check if it matches with the VMs one
    int file_version;
    fread(&file_version,sizeof(int), 1, file_pointer);
    if(file_version != VERSION){
        fprintf(stderr,"File and VM versions dont match\n");
        fclose(file_pointer);
        return;
    }

    //reading the number of instructions from the file and allocating memory for it
    int number_of_instructions;
    fread(&number_of_instructions, sizeof(int), 1, file_pointer);
    unsigned int *instructionsMEM = malloc(number_of_instructions * sizeof(unsigned int));
    if(instructionsMEM == NULL){
        fprintf(stderr,"Memory for instructions could not be allocated !\n");
        fclose(file_pointer);
        //return;
        exit(1);
    }

    program_memory_size = number_of_instructions;

    //reading the number of variables in the static data area and allocating memory for it
    int number_of_variables;
    fread(&number_of_variables, sizeof(int), 1, file_pointer);
    ObjRef *variablesMEM = malloc(number_of_variables * sizeof(ObjRef));
    if(variablesMEM == NULL){
        fprintf(stderr,"Memory for variables could not be allocated !\n");
        fclose(file_pointer);
        //return;
        exit(1);
    }
    num_of_glob_vars = number_of_variables;
    //giving sda the needed size specified in the binary and allocated 
    sda = variablesMEM;

    //initializing the sda with nil vlaues
    for(int i = 0; i < num_of_glob_vars; i++){
        sda[i] = NULL;
    }

    //reading the rest of the file into instructions memory
    fread(instructionsMEM, sizeof(unsigned int), number_of_instructions,file_pointer);
    fclose(file_pointer);

    pc = 0;
    sp = 0;
    fp = 0;
    //memcpy(programm_memory, instructionsMEM, number_of_instructions * sizeof(unsigned int));
    program_memory = instructionsMEM;


    printf("Ninja Virtual Machine started\n");
    //print_program();
    start_program_new();
    printf("Ninja Virtual Machine stopped\n");

    free(instructionsMEM);
    free(variablesMEM);
}

//Method for handling everything debugger-related
void debugger(){
    //variable for checking if the debugger is still running
    int still_running = 1;
    char command[20]; 
    int runs = 1;
    int breakpoint = -1;

    while(still_running == 1){
        printf("DEBUG: inspect(i), list(l), breakpoint(b), step(s), run(r), quit(q) ?\n");
        scanf("%1s", command);


        if(strcmp(command, "i") == 0){ //going into the inspect options

            printf("DEBUG [inspect]: stack(s), data(d), object(o) ?");
            scanf("%1s", command);

            if(strcmp(command, "s") == 0){// inspect -> stack

                debug_stack();

            }else if(strcmp(command, "d") == 0){// inspect -> static data area

                for(int i = 0; i<num_of_glob_vars;i++){
                    printf("static data area[%d]: %p\n",i,(void *)sda[i]);
                }

            }else if(strcmp(command, "o") == 0){
                ObjRef object;
                scanf("%p", (void **)&object);
                inspect_object(object);
            }
        }else if(strcmp(command, "l") == 0){// listing the whole program at once

            print_program();

        }else if(strcmp(command, "b") == 0){
            
            
            if(breakpoint == -1){
                printf("DEBUG [breakpoint]: cleared\n");
            }else{
                printf("DEBUG [breakpoint]: %d\n", breakpoint);
            }
            printf("DEBUG [breakpoint]:set an adress | -1 to reset adress | any character for no changes ?\n");
            if(scanf("%d", &breakpoint) == 1){
                printf("DEBUG [breakpoint]: now set at %d\n", breakpoint);
            }
            
               

        }else if(strcmp(command, "s") == 0){// taking a step within the program (instructionwise) this is where the debugger controls the programm course

            unsigned int instruction = program_memory[pc];
            print_instruction(instruction, pc);
            pc++;
            execute(instruction);

            int opcode = instruction >> 24;
            
            if(opcode == HALT){
                return;
            }
        }else if(strcmp(command, "r") == 0){// simply running the whole program until halt is met

            while(runs == 1){

                //print the breakpoint every time the programm passes it. PROBLEM: currently cannot show debugger options after each breakpoint pass
                if(breakpoint >= 0 && pc == breakpoint){
                    print_instruction(program_memory[pc], pc);
                    
                }
               
                unsigned int instruction = program_memory[pc];
                pc++;
                execute(instruction);

                int opcode = instruction >> 24;
                if(opcode == HALT){
                    runs = 0;
                    return;
                }
            }

           

        
        }
        else if(strcmp(command, "q") == 0){// quitting the debugger enteriley
            still_running = 0;
        }
    }
   
}



void cmd_args(int argc, char *argv[]){

    int size_set_stack = 0;
    int size_set_heap = 0;
    

    // case if there is only one argument
    if (argc < 2) {
        fprintf(stderr, "Error: no code file specified\n");
        return;  
    }

    // argc is the number of arguments within the argv Vector. 
    // So you can just iterate with a loop and get all arguments like that
    // the index variable starts at 1 so "./njvm" can be ignored while checking the other arguments
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--help") == 0) {
            printf("Usage: ./njvm [options] <code file>\nOptions:\n  --debug          start virtual machine in debug mode\n  --version        show version and exit\n  --help           show this help and exit\n");
            return;

        } else if (strcmp(argv[i], "--version") == 0) {
            printf("Ninja Virtual Machine version %d (compiled " __DATE__ " " __TIME__ ")\n", VERSION);
            return;

        }else if(strcmp(argv[i], "--debug") == 0){

           debug_mode = 1;

        }else if (strncmp(argv[i], "--stack", 7) == 0) {
            //checking if there is another argument
            if(i + 1 < argc){
                int stack_size = atoi(argv[i+1]); //atoi converts a string (ASCII) to an Integer, which in this case its the stack size
                if(stack_size <= 0){
                    fprintf(stderr,"Error: illegal stack size\n");
                    exit(1);
                }
                make_stack(stack_size);
                size_set_stack = 1; //setting size as set by user
                i++; // responsible for skipping the next argument because thats the stack size
            }

        }else if(strncmp(argv[i], "--heap", 6) == 0){
            
             //checking if there is another argument
            if(i + 1 < argc){
                int heap_size = atoi(argv[i+1]); //atoi converts a string (ASCII) to an Integer, which in this case its the stack size
                if(heap_size <= 0){
                    fprintf(stderr,"Error: illegal heap size\n");
                    exit(1);
                }

            make_heap(heap_size);
            size_set_heap = 1;
            i++; //responsible for skipping the next argument because thats the stack size
            }
        }else if(strncmp(argv[i], "--gcpurge", 9) == 0){ 
            gc_flag = 1;

        }else if(strncmp(argv[i], "--", 2) == 0){ //checking if the string in argv[i] begins with a "--", so checking if the first two chars match "--"
            printf("unknown command line argument '%s', try './njvm --help' \n", argv[i]);
        }else{
           
            if(!size_set_stack){
                make_stack(64); //default value
            }

            if(!size_set_heap){
                make_heap(8192); //default value
            }

            execute_binary(argv[i]);
        

            return;
        }
    }

    


}





int main(int argc, char *argv[]) {

    cmd_args(argc, argv);

   

    return 0;
}
