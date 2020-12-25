bp.registerBThread("bt-world",function(){
    bp.sync({waitFor:bp.Event("hello")});
    bp.sync({request:bp.Event("aba")});
    var x = 10;
    var y = 20;
    y = 21;
    bp.sync({request:bp.Event("world12121")});
})

bp.registerBThread("bt-hello", function(){
    bp.sync({request:bp.Event("hello")});
    var z = 11;
    var y = 40;
    y = 41;
    bp.sync({request:bp.Event("hello12312312")});

})