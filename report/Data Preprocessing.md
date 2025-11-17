## Data Preprocessing

Before truly importing the data into the relational database designed in TASK 2, we preprocess the data in the three csv files and identified several data quality issues that might lead to incosistencies or meaningless. To ensure the correctness and reliablity of subsequent steps, it is necessary to preprocess the data and normalize it before importing into database. So This section focus on these three aspects of preprocessing: deleting confused or meaningless records, correcting inconsistent attributes using other fields within the same record, and resolving data type problems.

The first problem is confused or meaningless records.// 这部分最好是解决了导入存在的一系列问题之后再去完成

这是数据导入的第一个问题，对应神秘magic number



The second problem is attributes that contradict other attributes within the same line. This issue may appear in `recipe.csv`. Some lines may have both CookTime and PrepareTime in correct form, but the TotalTime is either missing or inconsistent with the sum of two. The data preprocess procedures will treat CookTime and PrepareTime as correct and check the TotalTime. If the value is missing, it will be filled with the sum of other two times. However, if the value is inconsistent, we will use the sum of others to fill the blank instead of collisioned one. // 观众的数目那里也会有这样的预处理，可以写

这是数据导入的第二个问题：对不上



The third problem includes data type errors. In reviews.csv, every RecipeId column has the incorrent data type with the attached ".0", but the IDs should be integer type. So the data preprocess in this step should convert them into integers and remove the zero in the end. Moreover, the time-relavant   data follows ISO 8601 standard, with capital characters like P, T, H and so on. It should be the type `Time` or `Inteval` in SQL database, so we need to convert it into correct form as well. In our procedures, we decide to finish this convert step when inserting and parse it in Java program instead of preprocess stage.

In practical part, we used a python script to clean and preprocess row csv files and fixed all the problems mentioned previously. The source code will be attached as `data preprocess.py` file. There might be other strange data that haven't been covered, which will be processed in the following inserting part.



