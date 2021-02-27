bp.registerBThread("bt-world",function(){
    var tal = [5,3];
    foo();
    var xxxworld = 10;
    var yyyworld = 20;
    bp.sync({waitFor:bp.Event("hello")});
    bp.sync({request:bp.Event("aba")});
    foo()
    bp.sync({request:bp.Event("world12121")});
})

bp.registerBThread("bt-hello", function(){
    var zzzhello = 11;
    var yyyhello = 40;
    bp.sync({request:bp.Event("hello")});
    foo()
    bp.sync({request:bp.Event("hello12312312")});
})

function foo() {
    var foovar = 30;
    bp.sync({request:bp.Event("aba")});
    var m = 50;
    var n = 100;
    goo();
    var p = m+n;
    const t = 200
}

function goo() {
    var tt = 123;
    var qqq = 111;
    bp.sync({request:bp.Event("bbbb")});
}