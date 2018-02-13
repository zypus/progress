# Progress

[![Bintray](https://img.shields.io/bintray/v/zypus/github/progress.svg)]()
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This tiny library can be used to show some kind of progress indicator while executing some long running task. Especially, while writing small kotlin scripts it is tedious to reinvent a minimal progress indicator. Therefore this library provide an easy yet flexible interface for progress control. Further, in the case of simply printing to a console, a couple of pre-formatted values are at hand to compose a progress statement quickly: Like a progress bar, the elapsed and remaining time etc. (see complete set of features below).

## Acknowledgement

This library was inspired by the R package [progress](https://cran.r-project.org/web/packages/progress/index.html). 

## Example

```kotlin
import com.zypus.progress.Progress

fun main() {
    // Create a control with 100 ticks.
    val control = Progress.control(100L) {
        // On each update print (reprint) an informative statement about the progress.
        print("\r [$bar] $percent eta: $eta")
    }
    
    // Do the work.
    for (i in 1..100) {
        // Some expensive task.
        control.tick()
    }
}
```

The output of this small program will look something like this:
```
[======--------------]  30.0% eta: 10s
```

## Features

### Pre-formatted values

This is the complete set of pre-formatted features available in the progress update. (Of course the raw values are also available.)

Property   | Type   | Example   | Note
-----------|--------|-----------|-----
current    | String | 556       |
total      | String | 1000      |
percent    | String | 45.3%     |
bar        | String | ====----  | There is also a function bar() to customise the style of the bar.
spin       | String | /         | A string that changes each update and simulates a spinning element. Useful to indicate activity while no progress is made or the progress is unknown.
elapsed    | String | 10s       |
eta        | String | 1m25s     | Estimate by linear interpolation.
bytes      | String | 5.4kB     |
totalBytes | String | 100MB     |
rate       | String | 12.4kB/s  |
custom     | T?     | step 3 | Can be any generic class.
currentTicks | Long | 556  | Raw value.
totalTicks   | Long | 1000 | Raw value.
duration     | Duration |  | Raw value.

### Extension functions

For convenience this library adds extension functions for Collections, Iterables, Sequences and for reading files to minimise the boilerplate even further:

```kotlin
import com.zypus.progress.forEachBlockWithProgress

val file: java.io.File = // some File

// Read and process a potentially huge file.
file.forEachBlockWithProgress { buffer, bytesRead ->
	// process the file
}
```

And get output like this for free:
```
(/)   6.0s [===========---------]  55.0% (  2.27MB/4.12MB @ 378.2kB/s) eta:   5.0s
```

## Installation

To get started simply add it as a dependency to the maven repository at (https://dl.bintray.com/zypus/github):

### Gradle

```groovy
compile "com.zypus:progress:1.0.0"
```
    
### Maven

```xml
<dependency>
   <groupId>com.zypus</groupId>
   <artifactId>progress</artifactId>
   <version>1.0.0</version>
</dependency>
```

    
## Licence

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)