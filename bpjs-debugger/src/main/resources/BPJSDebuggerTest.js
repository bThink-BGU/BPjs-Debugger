bp.registerBThread("bt-world",function(){
    var beforefirstsync = 10;
    bp.sync({request:bp.Event("aba")});
    var tal = [5,3];
    foo(10);
    var xxxworld = 10;
    var yyyworld = 20;
    bp.sync({waitFor:bp.Event("hello")});
    bp.sync({request:bp.Event("aba")});
    foo(20)
    bp.sync({request:bp.Event("world12121")});
})

bp.registerBThread("bt-hello", function(){
    bp.sync({request:bp.Event("ima")});
    var zzzhello = 11;
    var yyyhello = 40;
    bp.sync({request:bp.Event("hello")});
    foo(30)
    bp.sync({request:bp.Event("hello12312312")});
})

function foo(fooparam) {
    var foovar = 1 +fooparam;
    var m = 50;
    bp.sync({request:bp.Event("OKINFOO")});
    var n = 100;
    goo();
    var p = m+n;
    const t = 200
}

function goo() {
    var tt = 123;
    var qqq = 111;
}