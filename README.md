# AnimeParserES
libreria para Parsear diferentes paginas de anime en español

Agregar esto a: **build.gradle(project)**
  

    allprojects {
      repositories {  
      google()  
            jcenter()  
            maven { url "https://jitpack.io" }  //Esto
     }}

Agregar esto a sus dependencias en: **build.gradle(app)**
  

    dependencies {  
	    implementation 'com.github.eme22:AnimeParserES:0.0.9'
    }
    
Ejemplo: 
  
        AnimeParserES parserES = AnimeParserES.getInstance();
        parserES.setBypassWebView(findViewById(R.id.bypassWebview)); //Webview para bypass sincronico (opcional)
        parserES.getAsync("https://www3.animeflv.net/anime/one-piece-tv", new AnimeParserES.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(Object animes) {
                if (animes instanceof Model){
                    //Trabaje con su anime o episodio
                }
                else if (animes instanceof WebModel){
                    //Trabaje si es una lista de animes
                }
            }

            @Override
            public void onError(AnimeError error) {
                    //Ha ocurrido Un error
            }
        });

