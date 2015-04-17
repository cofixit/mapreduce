# mapreduce
This is about creating a zoom animation on a mandelbrot set with a MapReduce Function in Hadoop.

## How to run this Map/Reduce Job
1. Clone this project
2. Run `./gradlew shadowJar`
3. Run `hadoop jar build/libs/mandelbrot-1.0.jar com.leon.mandelbrot.mapreduce.MandelbrotMapReduce`
It will show you an error message, but it will also teach you how to set the parameters for the job.

## How to import the project into IntlliJ
1. Clone this project into a folder
2. Import the project as a gradle proejct
3. Let IntelliJ do the magic

I haven't created an automatic build task for creating a jar and running it directly via hadoop jar. Sorry...
