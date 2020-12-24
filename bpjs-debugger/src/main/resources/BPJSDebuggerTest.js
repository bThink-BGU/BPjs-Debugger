bp.registerBThread("bt-world",function(){
    bp.sync({waitFor:bp.Event("hello")});
    bp.sync({request:bp.Event("aba")});
    var tal1 = 5;
    bp.sync({request:bp.Event("world12121")});
})

bp.registerBThread("bt-hello", function(){
    bp.sync({request:bp.Event("hello")});
    bp.sync({request:bp.Event("hello12312312")});

})