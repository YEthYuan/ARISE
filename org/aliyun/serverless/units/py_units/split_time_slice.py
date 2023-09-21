import json
import math
import os
from typing import *
import numpy as np
import datetime


def split(requests_file_path: str, meta_file_path: str, save_file_folder: str):
    def get_slices(timestamps: List[int], slice_in_ms):
        timestamps.sort()
        slice_start_time = 0
        slice_end_time = slice_start_time + slice_in_ms
        result = [0]
        index = 0
        while index < len(timestamps):
            if slice_start_time <= timestamps[index] < slice_end_time:
                result[-1] += 1
                index += 1
            else:
                result.append(0)
                slice_start_time += slice_in_ms
                slice_end_time += slice_in_ms
        return result

    def get_cumulative_dif(tar_list1: List[int], tar_list2: List[int], get_pre_dif: bool):
        result = [0]
        if get_pre_dif:
            tar_list2 = [0] + tar_list2
        for num1, num2 in zip(tar_list1, tar_list2):
            result.append(num1 - num2 + result[-1])
        return result[1:]

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

    min_time = min(assign_times_dict['all'])

    slice_in_ms_dict = {'all': 10000}
    with open(meta_file_path, 'r') as meta_file:
        for line in meta_file.readlines():
            meta_dict = json.loads(line)
            slice_in_ms_dict[meta_dict['key']] = meta_dict['initDurationInMs']

    for meta_key in assign_times_dict.keys():
        assign_times = np.asarray(assign_times_dict[meta_key]) - min_time
        idle_times = np.asarray(idle_times_dict[meta_key]) - min_time
        merge_times = np.asarray(assign_times_dict[meta_key] + idle_times_dict[meta_key]) - min_time

        assign_slices = get_slices(assign_times, slice_in_ms_dict[meta_key])
        with open(os.path.join(save_file_folder, f'{meta_key}_assign_{slice_in_ms_dict[meta_key]}.txt'), 'w') as save_file:
            timestamp = datetime.datetime.combine(datetime.date(2023, 8, 2), datetime.time(0, 0, 0))
            for slice in assign_slices:
                save_file.write(f'{timestamp.strftime("%Y-%m-%d %H:%M:%S")},{slice}\n')
                timestamp += datetime.timedelta(milliseconds=math.ceil(slice_in_ms_dict[meta_key] / 1000) * 1000)

        idle_slices = get_slices(idle_times, slice_in_ms_dict[meta_key])
        with open(os.path.join(save_file_folder, f'{meta_key}_idle_{slice_in_ms_dict[meta_key]}.txt'), 'w') as save_file:
            timestamp = datetime.datetime.combine(datetime.date(2023, 8, 2), datetime.time(0, 0, 0))
            for slice in idle_slices:
                save_file.write(f'{timestamp.strftime("%Y-%m-%d %H:%M:%S")},{slice}\n')
                timestamp += datetime.timedelta(milliseconds=math.ceil(slice_in_ms_dict[meta_key] / 1000) * 1000)

        merge_slices = get_slices(merge_times, slice_in_ms_dict[meta_key])
        with open(os.path.join(save_file_folder, f'{meta_key}_merge_{slice_in_ms_dict[meta_key]}.txt'), 'w') as save_file:
            timestamp = datetime.datetime.combine(datetime.date(2023, 8, 2), datetime.time(0, 0, 0))
            for slice in merge_slices:
                save_file.write(f'{timestamp.strftime("%Y-%m-%d %H:%M:%S")},{slice}\n')
                timestamp += datetime.timedelta(milliseconds=math.ceil(slice_in_ms_dict[meta_key] / 1000) * 1000)

        pre_dif_slices = get_cumulative_dif(assign_slices, idle_slices, True)
        with open(os.path.join(save_file_folder, f'{meta_key}_pre_dif_{slice_in_ms_dict[meta_key]}.txt'), 'w') as save_file:
            timestamp = datetime.datetime.combine(datetime.date(2023, 8, 2), datetime.time(0, 0, 0))
            for slice in pre_dif_slices:
                save_file.write(f'{timestamp.strftime("%Y-%m-%d %H:%M:%S")},{slice}\n')
                timestamp += datetime.timedelta(milliseconds=math.ceil(slice_in_ms_dict[meta_key] / 1000) * 1000)

        post_dif_slices = get_cumulative_dif(assign_slices, idle_slices, False)
        with open(os.path.join(save_file_folder, f'{meta_key}_post_dif_{slice_in_ms_dict[meta_key]}.txt'), 'w') as save_file:
            timestamp = datetime.datetime.combine(datetime.date(2023, 8, 2), datetime.time(0, 0, 0))
            for slice in post_dif_slices:
                save_file.write(f'{timestamp.strftime("%Y-%m-%d %H:%M:%S")},{slice}\n')
                timestamp += datetime.timedelta(milliseconds=math.ceil(slice_in_ms_dict[meta_key] / 1000) * 1000)


if __name__ == '__main__':
    split('/Users/tyx/Documents/Hackathon/data_training/dataSet_1/requests',
          '/Users/tyx/Documents/Hackathon/data_training/dataSet_1/metas',
          '/Users/tyx/Documents/Hackathon/slice_data/dataSet_1')
