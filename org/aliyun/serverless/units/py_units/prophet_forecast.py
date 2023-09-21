"""
使用facebook prophet时序预测
"""
import pandas as pd
from pandas import to_datetime

from prophet import Prophet
import matplotlib.pyplot as plt
import numpy as np

from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split

# roles1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/roles1_assign_25015.txt'
roles1_assign_path = '/Users/yeyuan/Desktop/dataSet_1/roles1_assign_25015.txt'
rolebindings1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/rolebindings1_assign_26707.txt'
certificate1_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_1/certificatesigningrequests1_assign_16964.txt'

roles2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/roles2_assign_26597.txt'
rolebindings2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/rolebindings2_assign_24389.txt'
certificate2_assign_path = '/Users/vanellope/Hackathon_File/machine_learning/prophet_data/dataSet_2/certificatesigningrequests2_assign_18329.txt'

roles1_assgin_model = Prophet(
                            changepoint_prior_scale=10000, # 跟随性, 越大越过拟合, default 0.05
                            n_changepoints=2000, # 转折点个数, default 25
                            interval_width=0.2, # 置信区间, 值越小, 上下线的带宽越小
                            growth='linear', # 'linear', 'logistic', default 'linear'
                            changepoint_range=0.9, # 0.8 默认从数据的前 80% 中寻找转折点
                            seasonality_mode='multiplicative', # 'additive', 'multiplicative'
                            seasonality_prior_scale=100000, # 季节性, 越大越过拟合, default 10
                            )
# TODO: 增加人为规定的holidays, 即特殊事件 

# 官方样例数据 https://github.com/facebook/prophet/blob/main/examples/example_retail_sales.csv
# roles1_assign_df = pd.read_csv('/Users/vanellope/Hackathon_File/machine_learning/prophet_data/example_yosemite_temps.csv', sep=',')
# roles1_assign_df.dropna(inplace=True)

# 读取 roles1_assign 数据 
roles1_assign_df = pd.read_csv(roles1_assign_path, header=None ,sep=',')

print(roles1_assign_df.head(n=10))
roles1_assign_df.columns = ['ds', 'y']
# 对y列使用math.log1p()进行变换
roles1_assign_df['y'] = np.log1p(roles1_assign_df['y'])
roles1_assign_df['ds'] = to_datetime(roles1_assign_df['ds'])


# split data, 不要打乱顺序
train, test = train_test_split(roles1_assign_df, test_size=0.2, shuffle=False)
print('train.tail: \n', train.tail(n=10))
print('test.head: \n', test.head(n=10))
print('Test request num: ', len(test))
# 画出原始数据
# train.set_index('ds').plot(figsize=(12, 8))
# plt.show()

# 拟合模型
roles1_assgin_model.fit(train)

# 预测全样本
y_pred = roles1_assgin_model.predict(roles1_assign_df)
y_true = roles1_assign_df['y'].values
y_pred = y_pred['yhat'].values
mse = mean_squared_error(y_true, y_pred)
print('MSE: ', mse)
plt.plot(y_true, label='All Actual')
plt.plot(y_pred, label='All Predicted')
plt.legend()
plt.show()

# 预测train数据
y_train_pred = roles1_assgin_model.predict(train)
y_train_true = train['y'].values
y_train_pred = y_train_pred['yhat'].values
mse = mean_squared_error(y_train_true, y_train_pred)
print('y_train_pred\n', y_train_pred[:10])
print('Train MSE: ', mse)
# fig = roles1_assgin_model.plot(y_train_pred)
# plt.show()

plt.plot(y_train_true, label='Train Actual')
plt.plot(y_train_pred, label='Train Predicted')
plt.legend()
plt.show()

# 预测test数据
y_test_pred = roles1_assgin_model.predict(test)
y_test_true = test['y'].values
y_test_pred = y_test_pred['yhat'].values
mse = mean_squared_error(y_test_true, y_test_pred)
print('Test MSE: ', mse)
# fig = roles1_assgin_model.plot(y_test_pred)
# plt.show()

plt.plot(y_test_true, label='Test Actual')
plt.plot(y_test_pred, label='Test Predicted')
plt.legend()
plt.show()
