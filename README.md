# D4T: Design for testability 

This repository contains the source code and replication package for our creative learning-based ‘design for testability’ approach in software systems. More information is available at [https://m-zakeri.github.io/D4T](https://m-zakeri.github.io/D4T).

@article{ZAKERINASRABADI2024107511,
title = {Measuring and improving software testability at the design level},
journal = {Information and Software Technology},
volume = {174},
pages = {107511},
year = {2024},
issn = {0950-5849},
doi = {https://doi.org/10.1016/j.infsof.2024.107511},
url = {https://www.sciencedirect.com/science/article/pii/S0950584924001162},
author = {Morteza Zakeri-Nasrabadi and Saeed Parsa and Sadegh Jafari},
keywords = {Software testability, Design for testability, Object-oriented design, Class diagram, Automated refactoring, Design patterns},
abstract = {Context
The quality of software systems is significantly influenced by design testability, an aspect often overlooked during the initial phases of software development. The implementation may deviate from its design, resulting in decreased testability at the integration and unit levels.
Objective
The objective of this study is to automatically identify low-testable parts in object-orientated design and enhance them by refactoring to design patterns. The impact of various design metrics mainly coupling (e.g., fan-in and fan-out) and inheritance (e.g., depth of inheritance tree and number of subclasses) metrics on design testability is measured to select the most appropriate refactoring candidates.
Method
The methodology involves creating a machine learning model for design testability prediction using a large dataset of Java classes, followed by developing an automated refactoring tool. The design classes are vectorized by ten design metrics and labeled with testability scores calculated from a mathematical model. The model computes testability based on code coverage and test suite size of classes that have already been tested via automatic tools. A voting regressor model is trained to predict the design testability of any class diagram based on these design metrics. The proposed refactoring tool for dependency injection and factory method is applied to various open-source Java projects, and its impact on design testability is assessed.
Results
The proposed design testability model demonstrates its effectiveness by satisfactorily predicting design testability, as indicated by a mean squared error of 0.04 and an R2 score of 0.53. The automated refactoring tool has been successfully evaluated on six open-source Java projects, revealing an enhancement in design testability by up to 19.11 %.
Conclusion
The proposed automated approach offers software developers the means to continuously evaluate and enhance design testability throughout the entire software development life cycle, mitigating the risk of testability issues stemming from design-to-implementation discrepancies.}
}
