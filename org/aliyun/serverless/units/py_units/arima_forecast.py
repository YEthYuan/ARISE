"""
使用ARIMA时序预测
"""
import pandas as pd
from pandas import to_datetime

from prophet import Prophet
import matplotlib.pyplot as plt
import numpy as np

from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split

from statsmodels.tsa.statespace.sarimax import SARIMAX

roles1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/roles1_assign_25015.txt'
rolebindings1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/rolebindings1_assign_26707.txt'
certificate1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/certificatesigningrequests1_assign_16964.txt'

roles2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/roles2_assign_.txt'
rolebindings2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/rolebindings2_assign_24389.txt'
certificate2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/certificatesigningrequests2_assign_18329.txt'


# TODO: 增加人为规定的holidays, 即特殊事件 

# 官方样例数据 https://github.com/facebook/prophet/blob/main/examples/example_retail_sales.csv
# data_df = pd.read_csv('/Users/vanellope/Hackathon_File/machine_learning/prophet_data/example_yosemite_temps.csv', sep=',')
# roles1_assign_df.dropna(inplace=True)

# 读取 roles1_assign 数据 
data_df = pd.read_csv(roles1_assign_path, header=None ,sep=',')

print(data_df.head(n=10))
data_df.columns = ['date', 'value']

# 转换时间格式
data_df['date'] = to_datetime(data_df['date'])

# 平滑原始数据, 取 sqrt(y)
# data_df['value'] = np.sqrt(data_df['value'])
# data_df['value'] = np.log1p(data_df['value'])
# 1阶差分
# data_df['value'] = data_df['value'].diff(1)
# 2阶差分
# data_df['value'] = data_df['value'].diff(2)
# data_df.dropna(inplace=True)

# split data, 不要打乱顺序
train, test = train_test_split(data_df, test_size=0.2, shuffle=False) # 不能shuffle, 否则会出现时间顺序错乱
print('train.tail: \n', train.tail(n=10))
print('test.head: \n', test.head(n=10))
print('Test request num: ', len(test))

"""
ARIMA模型的参数order表示的是模型中的p、d、q三个参数,分别代表自回归阶数、差分阶数和移动平均阶数。具体含义如下:

p(自回归阶数):表示用前p个时间步的数据来预测当前时间步。如果p=1,就是一阶自回归模型,用前一个时间步的数据来预测当前时间步。
d(差分阶数):表示数据需要差分的阶数,以使数据变得平稳。这是因为ARIMA模型在平稳数据上效果更好。如果原始数据已经平稳,d=0。如果需要一次差分才能使数据平稳,d=1 如果需要两次差分才能使数据平稳,d=2。
q(移动平均阶数):表示用前q个时间步的误差来预测当前时间步。如果q=1,就是一阶移动平均模型,用前一个时间步的误差来预测当前时间步。
因此,order是一个三元组(p,d,q)的形式,用于指定ARIMA模型的参数。例如,order=(2,1,1)表示使用2阶自回归、1阶差分和1阶移动平均的ARIMA模型。

seasonal_order 季节性自回归、差分和移动平均的阶数
"""

arima_model = SARIMAX(train['value'], order=(2, 1, 2), seasonal_order=(2, 1, 2, 7)) # seasonal_order=(0, 0, 0, 7) 一周七天季节性
# 拟合模型
result = arima_model.fit()
print(result.summary())

# 预测全样本
"""
start：预测开始的位置，可以是整数索引或日期时间对象，默认为0。
end：预测结束的位置，可以是整数索引或日期时间对象，默认为None，表示预测到最后一个时间步。
exog：外生变量数据，可以是一个DataFrame或一个二维数组，默认为None。
dynamic：控制预测时是否使用动态预测。如果dynamic=True，预测过程中使用模型的预测值替代真实值。如果dynamic=False，预测过程中使用真实值。默认为False。
(针对本任务，dynamic=False才有用)
"""
y_pred = result.predict(start=0, end=len(data_df)-1, dynamic=False)
y_true = data_df['value']
mse = mean_squared_error(y_true, y_pred)
print('MSE: ', mse)
plt.plot(y_true, label='All Actual')
plt.plot(y_pred, label='All Predicted')
plt.legend()
plt.show()

# 预测train数据
y_train_pred = result.predict(start=0, end=len(train)-1, dynamic=False)
y_train_true = train['value']
mse = mean_squared_error(y_train_true, y_train_pred)
print('Train MSE: ', mse)

plt.plot(y_train_true, label='Train Actual')
plt.plot(y_train_pred, label='Train Predicted')
plt.legend()
plt.show()

# 预测test数据
y_test_pred = result.predict(start=len(train), end=len(data_df)-1, dynamic=False)
y_test_true = test['value']
mse = mean_squared_error(y_test_true, y_test_pred)
print('Test MSE: ', mse)

plt.plot(y_test_true, label='Test Actual')
plt.plot(y_test_pred, label='Test Predicted')
plt.legend()
plt.show()
