bp.registerBThread("bt-world",function(){
    var beforefirstsync = 10;
    bp.sync({request:bp.Event("aba")});
    var afterfirstsync = 20;
    bp.sync({request:bp.Event("zzzworld"), waitFor:bp.Event("waittt")});

})

bp.registerBThread("bt-hello", function(){
    var beforefirstsync = 30;
    bp.sync({request:bp.Event("ima")});
    var afterfirstsync = 20;
    bp.sync({request:bp.Event("zzzhello"), waitFor:bp.Event("waitvv")});

})
