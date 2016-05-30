//------------------------------------------------------------------
eventStatusFinal = { msg ->
  println "==eventStatusFinal(${msg})=="
  growlNotify(msg)
}
 
eventStatusUpdate = { msg ->
  println "==eventStatusUpdate(${msg})=="
  growlNotify(msg)
}

growlNotify = { message ->
    println "==growlNotify(${message})=="
    return

    //path="/usr/local/bin/growlnotify"
    path="c:/opt/Growl/growlnotify.exe"
    if(!new File(path).exists())return
    imgpath="${basedir}/griffon-app/resources/griffon-icon-32x32.png"

    ant.exec(executable:path) {
          arg(value:"/t:Griffon")
          arg(value:"/i:\"${imgpath}\"")
          arg(value:"\"${message}\"")
    }
}


eventCompileStart = {msg->
  println "==compile start(${msg})=="
}

eventCompileEnd = {msg->
  println "==compile end(${msg})=="
  growlNotify("eventCompileEnd")

  libDir  = "${basedir}/lib/native"
  destDir = "${basedir}/staging/${griffon.util.PlatformUtils.platform}/native"
  ant.copy(todir: destDir) {
    fileset(dir: libDir, includes: '*.dll,*.so')
  }
}

eventPackageStart={msg->
  println "==package start(${msg})=="
  libDir  = "${basedir}/lib/native"
}

eventPackageEnd = {msg->
  println "==package end(${msg})=="
  growlNotify("eventPackageEnd")
}
