// Created by Bjorn Sandvik - thematicmapping.org
function Earth() {

	var webglEl = document.getElementById('webgl');

	if (!Detector.webgl) {
		Detector.addGetWebGLMessage(webglEl);
		return;
	}

	var width  = window.innerWidth,
		height = window.innerHeight;

    //users
	var users = [];
	// Earth params
	var radius   = 1,
		segments = 32,
		rotation = 180;
	var textspeed = 5000;

	var scene = new THREE.Scene();

	var camera = new THREE.PerspectiveCamera(45, width / height, 0.01, 1000);
	camera.position.z = 5;

	var renderer = new THREE.WebGLRenderer();
	renderer.setSize(width, height);

	scene.add(new THREE.AmbientLight(0xFFFFFF));


    var sphere = createSphere(radius, segments);
	sphere.rotation.y = rotation; 
	scene.add(sphere)

    var clouds = createClouds(radius, segments);
	clouds.rotation.y = rotation;
	scene.add(clouds)

	var stars = createStars(90, 64);
    scene.add(stars);

	var controls = new THREE.TrackballControls(camera, document.getElementById("webgl"));

	webglEl.appendChild(renderer.domElement);
		
	render();

	function render() {
		controls.update();
		//sphere.rotation.y += 0.0005;
		clouds.rotation.y += 0.0002;
		//light.rotation.y += 0.07
		
		//tube
		for (index = 0; index < users.length; ++index) {
			var messages = users[index].messages;
			if(messages.length > 0) {
				var tube = new THREE.TubeGeometry(users[index].connection, segments, 2, 3, false, false);
				for(index2 = 0; index2 < messages.length; ++ index2) {
					var message = messages[index2].message;
					var lastPosition = messages[index2].lastPosition;
					var time = Date.now() - messages[index2].creationDate;
					var isSend = messages[index2].send;
					var looptime = textspeed;
					var t = ( time % looptime ) / looptime;
					
					if(isSend) {
						var pos = tube.path.getPointAt(t);
					} else {
						var pos = tube.path.getPointAt(1-t);
						
					}
					if(lastPosition > t) {
						scene.remove(message);
						scene.remove(messages[index2].text);
						messages[index2].onEnd(users[index]);
						var toDelete = messages.indexOf(messages[index2]);
						if (toDelete > -1) {
 						   messages.splice(toDelete, 1);
						}
					} else {

                        if(messages[index2]) {
                        messages[index2].lastPosition = t;

                        message.position.x = pos.x;
                        message.position.y = pos.y;
                        message.position.z = pos.z;
                        //message.lookAt(camera.position);
                        messages[index2].text.position.x  = message.position.x + 0.03;
                        messages[index2].text.position.y  = message.position.y - 0.02;
                        messages[index2].text.position.z  = message.position.z;
                        messages[index2].text.lookAt(camera.position);
                        }
					}
					
				}
			}
		}

		requestAnimationFrame(render);
		renderer.render(scene, camera);
	}

	function createLineSphereAt(point, color) {
		var geometry = new THREE.SphereGeometry(0.010, 8, 8);
		
		//gimme that glow
		var material = new THREE.MeshLambertMaterial( { color: color, ambient: color } );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(0,40,0);
		
		// SUPER SIMPLE GLOW EFFECT
		// use sprite because it appears the same from all angles
		/*
		var spriteMaterial = new THREE.SpriteMaterial(
		{ 
			map: new THREE.ImageUtils.loadTexture( 'images/glow.png' ), 
			useScreenCoordinates: false, 
			alignment: THREE.SpriteAlignment.center,
			color: color, 
			transparent: false, 
			blending: THREE.AdditiveBlending
		});
		var sprite = new THREE.Sprite( spriteMaterial );
		sprite.scale.set(0.02, 0.02, 1.0);
		mesh.add(sprite); // this centers the glow at the mesh
		*/
		mesh.position.x = point.x;
		mesh.position.y = point.y;
		mesh.position.z = point.z;
		
		//sphere.add( mesh );
		
		return mesh;
	}
	
	this.createConnection = function(id, cords, color) {
		var cp = createCurvePath(cords, start);
		
		var curvedLineMaterial =  new THREE.LineBasicMaterial({color: color, linewidth: 2});
		var curvedLine = new THREE.Line(cp.createPointsGeometry(100), curvedLineMaterial);
		scene.add(curvedLine);
		users.push({id : id, coords : cords, line : curvedLine, connection: cp,messages : [], color : color});
	}

	this.removeConnection = function(userid) {
	    for(var i = 0; i < users.length;i++) {
        	  if(users[i].id == userid) {
                   for(var z=0;z < users[i].messages.length;z++) {
                       scene.remove(users[i].messages[z].message);
                       scene.remove(users[i].messages[z].text);
                   }
                   scene.remove(users[i].line);
                   users.splice(i, 1);
        	  }
        }
	}
	
	function createText(text,color) {
		var material = new THREE.MeshPhongMaterial({color: color, ambient: color});
		var textGeom = new THREE.TextGeometry(text, {size: 0.05,
			height: 0.005,
			curveSegments: 2,
			font: "helvetiker"});
		var textMesh = new THREE.Mesh(textGeom, material);
		return textMesh;
	}

	this.newMessage = function(userid, text) {
	    for(var i = 0; i < users.length;i++) {
	        if(users[i].id == userid) {
                createMessage(users[i], text);
                break;
	        }
	    }
	}

	function createMessage(user, text) {
		var miniSphere = createLineSphereAt(start, user.color);		
		var textMesh = createText(text,user.color);
		
		scene.add(textMesh);
		scene.add(miniSphere);
		
		var onEnd = function() {
			for(var i = 0;i<users.length;i++) {
				var user2 = users[i];
				var miniSphere2 = createLineSphereAt(start, user.color);
				var textMesh2 = createText(text,user.color);
				scene.add(textMesh2);
				scene.add(miniSphere2);
				var message2 = {message : miniSphere2, text: textMesh2, send:false, lastPosition: 0, onEnd : function(fromUser) {
				    if(fromUser.id == yourId) {
				        var tc = new THREE.Color(user.color);

				        var messageElem = document.getElementById("messages")
				        messageElem.innerHTML += "<div style='color:rgb("+tc.r*255+","+tc.g*255+","+tc.b*255+");'>"+text.replace("<","&lt;").replace(">","&gt;")+"</div>";
				        messageElem.scrollTop = messageElem.scrollHeight;
				    }
				}, creationDate : Date.now()}
				user2.messages.push(message2);
			}
		}

		var message = {message : miniSphere, text: textMesh, send:true, lastPosition: 0, onEnd : function(fromUser) { onEnd(); }, creationDate : Date.now()}
		user.messages.push(message);
		
	}
	
	function createSphere(radius, segments) {
		return new THREE.Mesh(
			new THREE.SphereGeometry(radius, segments, segments),
			new THREE.MeshPhongMaterial({
				map:         THREE.ImageUtils.loadTexture('images/2_no_clouds_4k.jpg'),
				bumpMap:     THREE.ImageUtils.loadTexture('images/elev_bump_4k.jpg'),
				bumpScale:   0.005,
				specularMap: THREE.ImageUtils.loadTexture('images/water_4k.png'),
				specular:    new THREE.Color('grey')								
			})
		);
	}

	function createClouds(radius, segments) {
		return new THREE.Mesh(
			new THREE.SphereGeometry(radius + 0.003, segments*2, segments*2),			
			new THREE.MeshPhongMaterial({
				map:         THREE.ImageUtils.loadTexture('images/fair_clouds_4k.png'),
				transparent: true
			})
		);		
	}

	function createStars(radius, segments) {
                    return new THREE.Mesh(
                            new THREE.SphereGeometry(radius, segments, segments),
                            new THREE.MeshBasicMaterial({
                                    map: THREE.ImageUtils.loadTexture('images/galaxy_starfield.png'),
                                    side: THREE.BackSide
                            })
                    );
    }
	
	function createCurvePath(start, end) {
        var start3 = translateCordsToPoint(start.latitude,start.longitude);
		//start3.multiplyScalar(1.03)
        var end3 = translateCordsToPoint(end.latitude, end.longitude);
		//end3.multiplyScalar(1.03)
        var mid = (new LatLon(start.latitude,start.longitude)).midpointTo(new LatLon(end.latitude, end.longitude));
        var middle3 = translateCordsToPoint(mid.lat(), mid.lon(), 0);

        var curveQuad = new THREE.QuadraticBezierCurve3(start3, middle3, end3);
		//little hacky
		middle3 = translateCordsToPoint(mid.lat(), mid.lon(), curveQuad.getLength());
		curveQuad = new THREE.QuadraticBezierCurve3(start3, middle3, end3);
		
        var cp = new THREE.CurvePath();
        cp.add(curveQuad);
        return cp;
    }
	
	function createCurvePathCubic(start, end) {
        var start3 = translateCordsToPoint(start.latitude,start.longitude);
        var start3_control = start3.clone()
		start3_control.multiplyScalar(2)
		
		var end3 = translateCordsToPoint(end.latitude, end.longitude);
		var end3_control = end3.clone()
		end3_control.multiplyScalar(2)


        var curveCubic = new THREE.CubicBezierCurve3(start3, start3_control, end3_control, end3);
		
        var cp = new THREE.CurvePath();
        cp.add(curveCubic);
        return cp;
    }

	function translateCordsToPoint(lat, lng, elevation) {
        // toRad()
        var phi = (90 /*- 2.5*/ - lat) * Math.PI / 180;
        var theta = (126.5 - lng) * Math.PI / 180; // texture

        if(!elevation) elevation = 0;

        var point = new THREE.Vector3();
        point.x = (radius + elevation) * Math.sin(phi) * Math.cos(theta);
        point.y = (radius + elevation) * Math.cos(phi);
        point.z = (radius + elevation) * Math.sin(phi) * Math.sin(theta);

        return point;
    }
};