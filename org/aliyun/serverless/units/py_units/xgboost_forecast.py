"""
使用方法
python xgboost_forecast.py --dataset_num 1 --meta_key roles1 --request_type assign

xgboost machine learning for time-series forecasting
训练输入 X 是一个多列 dataframe, 输出 y 的列数等于 X 的列数/使用历史时间片个数
其中, 设计一下两种训练方法:
    - X.columns=[t-3, t-2, t-1], y.columns=[t], 即训练特征不按照 metaKeys分类, 而是按照时间片分类
    - X.columns=[key1(t-3), key1(t-2), key1(t-1), key2(t-3), key2(t-2), key2(t-1), ...], y.columns=[key1(t), key2(t), key3(t)]
"""
import argparse
import pandas as pd
from pandas import DataFrame
import numpy as np
import matplotlib.pyplot as plt
import xgboost as xgb
from xgboost import XGBRegressor
from sklearn.multioutput import MultiOutputRegressor
from sklearn.metrics import mean_squared_error, explained_variance_score
from sklearn.model_selection import train_test_split, GridSearchCV

my_path = '/Users/vanellope/Hackathon_File/machine_learning' # slice_data 上层目录, 即 split_time_slice.py 文件输出目录

data_file_path_dict = {
	"roles1_assign_path" : my_path +'/slice_data/dataSet_1/roles1_assign_sliceMs_25015.txt',
	"roles1_idle_path" : my_path + '/slice_data/dataSet_1/roles1_idle_sliceMs_25015.txt',
	"rolebindings1_assign_path" : my_path + '/slice_data/dataSet_1/rolebindings1_assign_sliceMs_26707.txt',
	"rolebindings1_idle_path" : my_path + '/slice_data/dataSet_1/rolebindings1_idle_sliceMs_26707.txt',
	"certificatesigningrequests1_assign_path" : my_path + '/slice_data/dataSet_1/certificatesigningrequests1_assign_sliceMs_16964.txt',
	"certificatesigningrequests1_idle_path" : my_path + '/slice_data/dataSet_1/certificatesigningrequests1_idle_sliceMs_16964.txt',

	"roles2_assign_path" : my_path + '/slice_data/dataSet_2/roles2_assign_sliceMs_26597.txt',
	"roles2_idle_path" : my_path + '/slice_data/dataSet_2/roles2_idle_sliceMs_26597.txt',
	"rolebindings2_assign_path" : my_path + '/slice_data/dataSet_2/rolebindings2_assign_sliceMs_24389.txt',
	"rolebindings2_idle_path" : my_path + '/slice_data/dataSet_2/rolebindings2_idle_sliceMs_24389.txt',
	"certificatesigningrequests2_assign_path" : my_path + '/slice_data/dataSet_2/certificatesigningrequests2_assign_sliceMs_18329.txt',
	"certificatesigningrequests2_idle_path" : my_path + '/slice_data/dataSet_2/certificatesigningrequests2_idle_sliceMs_18329.txt',
}
########### 数据文件路径 ############

def series_to_supervised(data: DataFrame, n_in=1, n_out=1, dropnan=True):
	"""
	将时间序列数据转换为监督学习数据, 即把该时间片的数值作为特征 X, 下一个时间片的数值作为标签 y
	Arguments:
		data: 带有多个 metaKey 的序列, dataframe
		n_in: 历史时间片数量, Number of lag observations as input (X) .
		n_out: 未来时间片数量, Number of observations as output (y).
		dropnan: Boolean whether or not to drop rows with NaN values.
	Returns:
		Pandas DataFrame of series framed for supervised learning.
	"""
	n_vars = 1 if type(data) is list else data.shape[1]
	var_names = list(data.columns)

	df = data
	cols, names = list(), list()
	# input sequence (t-n, ... t-1)
	for i in range(n_in, 0, -1):
		cols.append(df.shift(i))
		# names += [('var%d(t-%d)' % (j+1, i)) for j in range(n_vars)]
		names += [('%s(t-%d)' % (var_name, i)) for var_name in var_names]
	# forecast sequence (t, t+1, ... t+n)
	for i in range(0, n_out):
		cols.append(df.shift(-i))
		if i == 0:
			# names += [('var%d(t)' % (j+1)) for j in range(n_vars)]
			names += [('%s(t)' % (var_name)) for var_name in var_names]
		else:
			# names += [('var%d(t+%d)' % (j+1, i)) for j in range(n_vars)]
			names += [('%s(t+%d)' % (var_name, i)) for var_name in var_names]
	# put it all together
	agg = pd.concat(cols, axis=1)
	agg.columns = names
	# drop rows with NaN values
	if dropnan:
		agg.dropna(inplace=True)
	return agg

def gen_train_dataset_from_series(roles_path, rolebindings_path, certificate_path, n_in, n_out, choose_key, dataset_num) -> DataFrame:
	"""
	目前对于 dataset1/dataset2, 仅考虑 roles, rolebindings, certificate 这三种 metakey
	"""
	role_assign_series = pd.read_csv(roles_path, sep='\t', header=None)
	rolebindings_assign_series = pd.read_csv(rolebindings_path, sep='\t', header=None)
	certificatesigningrequests_assign_series = pd.read_csv(certificate_path, sep='\t', header=None)
	
	if dataset_num == 1:
		role_assign_series.columns = ['roles1']
		rolebindings_assign_series.columns = ['rolebindings1']
		certificatesigningrequests_assign_series.columns = ['certificatesigningrequests1']
	elif dataset_num == 2:
		role_assign_series.columns = ['roles2']
		rolebindings_assign_series.columns = ['rolebindings2']
		certificatesigningrequests_assign_series.columns = ['certificatesigningrequests2']
	else:
		print("dataset_num error!, please input 1 or 2")
    
	# concat 3 meta timeline series
	concat_assign_series = pd.concat([role_assign_series, rolebindings_assign_series, certificatesigningrequests_assign_series], axis=1)
	print("concat assign series\n", concat_assign_series)
	concat_assign_series = concat_assign_series[choose_key]
	concat_assign_series.to_csv('/Users/vanellope/Hackathon_File/machine_learning/slice_data/concat_assign_series_data%d.csv' % dataset_num, index=False)
	
	data = series_to_supervised(concat_assign_series, n_in=n_in, n_out=n_out, dropnan=True)
	return data

def gen_dataset_from_series(filepath: str, n_in:int, n_out:int, metaKey: list[str]) -> DataFrame:
	"""
	一个series文件生成一个dataset, 仅考虑单个 metakey
	"""
	df = pd.read_csv(filepath, sep='\t', header=None)
	assert type(df) == DataFrame
	df.columns = [metaKey]
	
	data = series_to_supervised(df, n_in=n_in, n_out=n_out, dropnan=True)
	print(data)
	return data

def evaluate_model(model, X, y, X_train, y_train, X_test, y_test):
	# 评估完整样本集
	y_pred = np.floor(model.predict(X))
	y_true = y.to_numpy().flatten()
	mse = mean_squared_error(y_true, y_pred)
	assert len(y_true) == len(y_pred)
	print('type(y_true): ', type(y_true), y_true.shape)
	print('type(y_pred): ', type(y_pred), y_pred.shape)
	# print('y_true\n', y_true[300:400])
	# print('y_pred\n', y_pred[300:400])
	print('rmse: %.5f' % np.sqrt(mse))
	print('explained_variance_score: ', explained_variance_score(y_true, y_pred))
	# add transpance to plot
	# plt.plot(y_true, label='y_true',)

	# plt.plot(y_true, label='y_true', color='red', linewidth=1.0, linestyle='--', alpha=0.5)
	# plt.plot(y_pred, label='y_pred', color='blue', alpha=0.5)
	# plt.legend()
	# plt.show()

	# 评估完整训练集
	y_train_pred = np.floor(model.predict(X_train))
	y_train_true = y_train.to_numpy().flatten()
	mse = mean_squared_error(y_train_pred, y_train_true)
	# print('y_train_true\n', y_train_true[50:100])
	# print('y_train_pred\n', y_train_pred[50:100])
	print('Train rmse: %.5f' % np.sqrt(mse))
	print('Train explained_variance_score: ', explained_variance_score(y_train_pred, y_train_true))
	# plt.plot(y_train_true, label='y_train_true', color='red', linewidth=1.0, linestyle='--', alpha=0.5)
	# plt.plot(y_train_pred, label='y_train_pred', color='blue', alpha=0.5)
	# plt.legend()
	# plt.show()
	

	# 评估测试集
	y_test_pred = np.floor(model.predict(X_test))
	y_test_true = y_test.to_numpy().flatten()
	mse = mean_squared_error(y_test_true, y_test_pred)
	# print('y_test_true\n', y_test_true[50:100])
	# print('y_test_pred\n', y_test_pred[50:100])
	print('Test rmse: %.5f' % np.sqrt(mse))
	print('Test explained_variance_score: ', explained_variance_score(y_test_true, y_test_pred))
	# plt.plot(y_test_true, label='y_test_true', color='red', linewidth=1.0, linestyle='--', alpha=0.5)
	# plt.plot(y_test_pred, label='y_test_pred', color='blue', alpha=0.5)
	# plt.legend()
	# plt.show()

def evaluate_loaded_model(loaded_model, X_data, y_data, data_type:str):
	X = xgb.DMatrix(X_data)
	y_pred = np.floor(loaded_model.predict(X))
	y_true = y_data.to_numpy().flatten()
	mse = mean_squared_error(y_true, y_pred)
	assert len(y_true) == len(y_pred)
	print(data_type + ' rmse: %.5f ' % np.sqrt(mse))
	
if __name__=='__main__':
	# 创建解析器对象
	parser = argparse.ArgumentParser()

    # 添加参数--dataset_num 1 --meta_key roles1
	parser.add_argument("--dataset_num", help="dataSet number")
	parser.add_argument("--meta_key", help="meta key name, like roles1")
	parser.add_argument("--request_type", help="assign or idle")
	# 解析命令行参数
	args = parser.parse_args()
	dataset_num = args.dataset_num
	meta_key = args.meta_key
	request_type = args.request_type
	data_file_path_key = meta_key + '_' + request_type + '_path'
	data_file_path = data_file_path_dict[data_file_path_key]
    
	########### 模型超参数 ############
	n_in = 20 # 利用前 n_in 个时间片的数据作为特征, 10-50, best 20
	n_out = 1 # 单步预测 n_out = 1, 多步预测 n_out > 1
	
	other_params = {  # for dataset1 roles1_assign best
		'objective': 'reg:squarederror', # 损失函数, same as 'reg:linear'
		'n_estimators': 200,
		'max_depth': 14,
		'min_child_weight': 1, # 叶子结点最小权重, 用于控制过拟合, 越大越保守
		'gamma': 0, # 惩罚项中叶子结点个数前的参数, γ设定越大，树的叶子数量就越少，模型的复杂度就越低
		'subsample': 1,  
		'colsample_bytree': 1,
		'reg_alpha': 0,  # L1正则化系数, default 0, 
		'reg_lambda': 0.4, # L2正则化系数,  default 1
		'learning_rate': 0.05, 
		'verbosity': 1, # 打印消息的详细程度, 0 (silent), 1 (warning), 2 (info), 3 (debug)
	}
	
	
	grid_serach_params = { # 按一下排列顺序调参
		# 'n_estimators': [90, 100, 110, 120, 130, 140, 150, 200], 
		# 'max_depth': [6, 7, 8, 9, 10, 11, 12],
		# 'min_child_weight': [1, 2, 3, 4, 5, 6],
		# 'gamma': [0, 0.1, 0.2, 0.5],
		# 'subsample': [0.7, 0.8, 0.9, 1],
		# 'colsample_bytree': [0.7, 0.8, 0.9, 1],
		# 'reg_alpha': [0, 0.1, 0.2, 0.3, 0.4],
		# 'reg_lambda': [0.4, 0.6, 0.8, 1],
		# 'learning_rate': [0.01, 0.05, 0.1],
	}
	choose_best_estimator = False

	
	# 读取+构造数据集, 输入输出的个数可以根据需要进行调整
	train_data = gen_dataset_from_series(filepath=data_file_path, n_in=n_in, n_out=n_out, metaKey=meta_key)
	print(train_data)
	
	######### 参数调优 GridSearchCV #########
	single_XGB_model = XGBRegressor(**other_params)
	grid_serach_model = GridSearchCV(single_XGB_model, 
				  					grid_serach_params,
									refit=True, # 在搜索参数结束后, 用最佳参数结果再次fit一遍全部数据集
									cv=5, # 交叉验证次数 k, 每次取 1/k 的数据作为验证集, 其余为训练集, [3, 5, 10] best 5
									scoring='neg_mean_squared_error', # 评价指标
									verbose=2, # 打印消息的详细程度, 0 (silent), 1 (warning), 2 (info), 3 (debug)
									n_jobs=-1) # n_jobs=-1 使用所有的CPU进行训练, n_jobs=1 只使用一个CPU


	X = train_data.iloc[:, :-n_out]
	y = train_data.iloc[:, -n_out:]
	print(X) # feature
	print(y) # label
	X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=28) # random_state=28
	# print('X_train\n', X_train)
	# print('y_train\n', y_train)
	# print('X_test\n', X_test)
	# print('y_test\n', y_test)
	
	print('single_XGB_model fitting...')
	single_XGB_model.fit(X_train, y_train)

	
	print('grid_serach_model fitting...')
	grid_serach_model.fit(X, y)
	print('best_params_', grid_serach_model.best_params_)
	print('best_score_', grid_serach_model.best_score_)
	best_XGB_model = grid_serach_model.best_estimator_
	
	print('Eval single_XGB_model...')
	evaluate_model(single_XGB_model, X, y, X_train, y_train, X_test, y_test)

	print('Eval best_XGB_model...')
	evaluate_model(best_XGB_model, X, y, X_train, y_train, X_test, y_test)
	
	######## TODO: 保存模型 ########
	# single_XGB_model.save_model('/Users/vanellope/Hackathon_File/machine_learning/best_model_bin/roles1_assign_model.bin')

	output_path = '/Users/vanellope/Hackathon_File/machine_learning/best_model_bin'
	best_XGB_model.save_model(output_path + f'/dataSet_{dataset_num}_model/{meta_key}_{request_type}_best_model.bin')




	# load model to predict看结果是否有偏差
	# loaded_model = xgb.Booster()
	# loaded_model.load_model('/Users/vanellope/Hackathon_File/machine_learning/best_model_bin/roles1_assign_best_model.bin')
	# evaluate_loaded_model(loaded_model, X, y, 'all')
	# evaluate_loaded_model(loaded_model, X_train, y_train, 'train')
	# evaluate_loaded_model(loaded_model, X_test, y_test, 'test')