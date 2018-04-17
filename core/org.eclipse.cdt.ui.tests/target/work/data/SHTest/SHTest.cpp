#define INT      int                               //$macroDefinition
#define FUNCTION_MACRO(arg) globalFunc(arg)        //$macroDefinition
#define EMPTY_MACRO(arg)                           //$macroDefinition
#include "SHTest.h"
enum Enumeration {                                 //$enum
    enumerator                                     //$enumerator
};

const int globalConstant = 0;                      //$globalVariable
int globalVariable = 0;                            //$globalVariable
static int globalStaticVariable = 0;               //$globalVariable

void globalFunc(int a);                            //$functionDeclaration,parameterVariable
static void globalStaticFunc() {                   //$functionDeclaration
    EMPTY_MACRO(n);                                //$macroSubstitution
};

class Base1 {};                                    //$class
class Base2 {};                                    //$class

class ClassContainer : Base1, Base2 {              //$class,class,class
    friend void friendFunc();                      //$functionDeclaration
    friend class FriendClass;                      //$class
    
public:
    static int staticPubField;                     //$staticField
    const int constPubField;                       //$field
    const static int constStaticPubField;          //$staticField
    int pubField;                                  //$field

    static int staticPubMethod(int arg) {          //$methodDeclaration,parameterVariable
        FUNCTION_MACRO(arg);                       //$macroSubstitution,parameterVariable
        globalFunc(arg);                           //$function,parameterVariable
        return globalStaticVariable;               //$globalVariable
    }
    int pubMethod();                               //$methodDeclaration

    enum pubEnumeration {pubEnumerator};           //$enum,enumerator
    class pubClass{};                              //$class
    class pubStruct{};                             //$class
    class pubUnion{};                              //$class
    typedef pubClass pubTypedef;                   //$class,typedef
    
protected:
    static const int constStaticProtField = 12;    //$staticField
    static int staticProtField;                    //$staticField
    const  int constProtField;                     //$field
    int protField;                                 //$field

    static int staticProtMethod();                 //$methodDeclaration
    int protMethod();                              //$methodDeclaration

    enum protEnumeration {protEnumerator};         //$enum,enumerator
    class protClass{};                             //$class
    class protStruct{};                            //$class
    class protUnion{};                             //$class
    typedef protClass protTypedef;                 //$class,typedef
    
private:
    static const int constStaticPrivField = 12;    //$staticField
    static int staticPrivField;                    //$staticField
    const  int constPrivField;                     //$field
    int privField;                                 //$field

    static int staticPrivMethod();                 //$methodDeclaration
    int privMethod();                              //$methodDeclaration

    enum privEnumeration {privEnumerator};         //$enum,enumerator
    class privClass{};                             //$class
    class privStruct{};                            //$class
    class privUnion{};                             //$class
    typedef privClass privTypedef;                 //$class,typedef


};

template<class T1, class T2> class TemplateClass { //$templateParameter,templateParameter,class
    T1 tArg1;                                      //$templateParameter,field
    T2 tArg2;                                      //$templateParameter,field
    TemplateClass(T1 arg1, T2 arg2) {              //$methodDeclaration,templateParameter,parameterVariable,templateParameter,parameterVariable
        tArg1 = arg1;                              //$field,parameterVariable
        tArg2 = arg2;                              //$field,parameterVariable
    }
};

template<class T1> class PartialInstantiatedClass  //$templateParameter,class
    : TemplateClass<T1, Base1> {};                 //$class,templateParameter,class


struct CppStruct {                                 //$class
    int structField;                               //$field
};

union CppUnion {                                   //$class
    int unionField;                                //$field
    CppUnion operator+(CppUnion);                  //$class,methodDeclaration,class
    CppUnion operator[](int);                      //$class,methodDeclaration
};

typedef CppUnion TUnion;                           //$class,typedef

namespace ns {                                     //$namespace
    int namespaceVar = 0;                          //$globalVariable
    int namespaceFunc() {                          //$functionDeclaration
	globalStaticFunc();                              //$function
	return namespaceVar;                             //$globalVariable
    }
}

INT ClassContainer::protMethod() {                 //$macroSubstitution,methodDeclaration
    return protField;                              //$field
}

INT ClassContainer::pubMethod() {                  //$macroSubstitution,methodDeclaration
    int localVar = 0;                              //$localVariableDeclaration
    return pubField + localVar;                    //$field,localVariable
}

INT ClassContainer::staticPrivMethod() {           //$macroSubstitution,methodDeclaration
    CppStruct* st= new CppStruct();                //$class,localVariableDeclaration,class
    st->structField= 1;                            //$localVariable,field
    CppUnion un;                                   //$class,localVariableDeclaration
    un.unionField= 2;                              //$localVariable,field
    staticPubMethod(staticPrivField);              //$staticMethod,staticField
    un + un[6];                                    //$localVariable,overloadedOperator,localVariable,overloadedOperator,overloadedOperator
label:                                             //$label
    FUNCTION_MACRO(0);                             //$macroSubstitution
    if (un.unionField < st->structField)           //$localVariable,field,localVariable,field
      goto label;                                  //$label
    problemMethod();                               //$problem
    // external SDK
    SDKClass sdkClass;                             //$class,localVariableDeclaration
    sdkClass.SDKMethod();                          //$localVariable,externalSDK
    SDKFunction();                                 //$externalSDK
    return 0;
}

//http://bugs.eclipse.org/209203
template <int n>                                   //$templateParameter
int f()                                            //$functionDeclaration
{
  return n;                                        //$templateParameter
}

//http://bugs.eclipse.org/220392
#define EMPTY                                      //$macroDefinition
EMPTY int f();                                     //$macroSubstitution,functionDeclaration

//http://bugs.eclipse.org/340492
template< template<class> class U >                //$templateParameter
class myClass {};                                  //$class

//http://bugs.eclipse.org/372004
void g() {                                         //$functionDeclaration
    // declared as global near top
    extern int globalVariable;                     //$globalVariable
}

//http://bugs.eclipse.org/399149
class C final {                                    //$class,c_keyword
    void finalMethod() final;                      //$methodDeclaration,c_keyword
    void overrideMethod() override;                //$methodDeclaration,c_keyword

    // ordinary field, happens to be named 'final'
    int final;                                     //$field
};
