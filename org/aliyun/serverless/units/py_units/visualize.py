import os.path
import matplotlib.pyplot as plt
import json
import seaborn as sns
import numpy as np


def visualize(requests_file_path: str):
    assign_times_dict = {'all': []}
    idle_times_dict = {'all': []}
    with open(requests_file_path, 'r') as requests_file:
        for line in requests_file.readlines():
            request_dict = json.loads(line)
            start_time = request_dict['startTime']
            meta_key = request_dict['metaKey']
            durations = request_dict['durationsInMs']

            if meta_key not in assign_times_dict.keys():
                assign_times_dict[meta_key] = []
            if meta_key not in idle_times_dict.keys():
                idle_times_dict[meta_key] = []

            assign_times_dict[meta_key].append(start_time)
            assign_times_dict['all'].append(start_time)
            idle_times_dict[meta_key].append(start_time + durations)
            idle_times_dict['all'].append(start_time + durations)

    for meta_key in assign_times_dict.keys():
        min_assign_time = min(assign_times_dict[meta_key])
        assign_times_dict[meta_key] = np.asarray(assign_times_dict[meta_key]) - min_assign_time
        idle_times_dict[meta_key] = np.asarray(idle_times_dict[meta_key]) - min_assign_time

    for i, meta_key in enumerate(assign_times_dict.keys()):
        # print(assign_times_dict[meta_key])
        plt.title(meta_key)
        fig = sns.displot(assign_times_dict[meta_key])
        fig.savefig(os.path.join('/Users/tyx/Documents/Hackathon/fig/dataSet_3', meta_key + '_assign.png'), dpi=400)
    for i, meta_key in enumerate(idle_times_dict.keys()):
        # print(idle_times_dict[meta_key])
        plt.title(meta_key)
        fig = sns.displot(idle_times_dict[meta_key])
        fig.savefig(os.path.join('/Users/tyx/Documents/Hackathon/fig/dataSet_3', meta_key + '_idle.png'), dpi=400)


if __name__ == '__main__':
    visualize('/Users/tyx/Documents/Hackathon/data_training/dataSet_3/requests')
