# SELA
### SimplE Lossless Audio
Java rewrite of the original [sela project](https://github.com/sahaRatul/sela).

### Block Diagrams
![Encoder](https://cloud.githubusercontent.com/assets/12273725/8868411/c24585e6-31f5-11e5-937a-e3c11c632704.png)
![Decoder](https://cloud.githubusercontent.com/assets/12273725/8868418/cbb6a1dc-31f5-11e5-91f6-8290766baa34.png)

### Build requirements
- Open/Oracle JDK 8/13
- Maven

### Build instructions
- cd to the directory
- type ```mvn package``` to build the project

### References
- Linear Prediction
  - [Wikipedia](https://en.wikipedia.org/wiki/Linear_prediction)
  - [Digital Signal Processing by John G. Proakis & Dimitris G. Monolakis](http://www.amazon.com/Digital-Signal-Processing-4th-Edition/dp/0131873741)
  - [A detailed pdf](http://www.ece.ucsb.edu/Faculty/Rabiner/ece259/digital%20speech%20processing%20course/lectures_new/Lecture%2013_winter_2012_6tp.pdf)
- Golomb-Rice lossless compression algorithm
  - [Wikipedia](https://en.wikipedia.org/wiki/Golomb_coding)
  - [Here is an implementation](http://michael.dipperstein.com/rice/index.html)
- [MPEG4 ALS overview](http://elvera.nue.tu-berlin.de/files/1216Liebchen2009.pdf)
- [FLAC overview](https://xiph.org/flac/documentation_format_overview.html)
- [Paper on shorten, the original open source lossless codec](ftp://svr-ftp.eng.cam.ac.uk/pub/reports/robinson_tr156.ps.Z)
- ISO/IEC 14496 Part 3, Subpart 11 (Audio Lossless Coding)

### License : MIT