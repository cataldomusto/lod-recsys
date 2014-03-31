import csv
import collections as co
import matplotlib.pyplot as plt
import numpy


class DatasetAnalyzer:
    def __init__(self, filename, delimiter=None):
        self._filename = filename
        self._prop_dict = co.defaultdict()
        self._user_list = set()
        self._delimiter = delimiter
        self._read_dataset_properties()

    def _read_dataset_properties(self):
        with open(self._filename, encoding='utf-8') as data_file:
            if self._delimiter is not None:
                csv_file = csv.reader(data_file, delimiter=self._delimiter)
            else:
                csv_file = csv.reader(data_file)

            for row in csv_file:
                if row[1] in self._prop_dict:
                    self._prop_dict[row[1]].append((row[0], row[2]))
                else:
                    self._prop_dict[row[1]] = [(row[0], row[2])]
                self._user_list.add(row[0])
        return self._prop_dict
    
    # returns the total number of element in the dataset
    def tot_entries(self):
        return sum([len(self._prop_dict[x]) for x in self._prop_dict])
    
    # total number of users in the dataset
    def tot_number_users(self):
        return len(self._user_list)
    
    # total number of items in the dataset
    def tot_number_items(self):
        return len(self._prop_dict)
    
    # items' positive ratings average of the whole dataset
    def posrate_average_item(self):
        return sum([sum([1 for tup in self._prop_dict[key] if tup[1] == '1']) 
                    for key in self._prop_dict])/self.tot_entries()
    
    # items' negative ratings average of the whole dataset    
    def negrate_average_item(self):
        return sum([sum([1 for tup in self._prop_dict[key] if tup[1] == '0']) 
                    for key in self._prop_dict])/self.tot_entries()
    
    # number of positive ratings received by a single item
    def _posrate_item(self, item):
        return len([1 for x in self._prop_dict[item] if x[1] == '1'])
    
    # number of negative ratings received by a single item
    def _negrate_item(self, item):
        return len([1 for x in self._prop_dict[item] if x[1] == '0'])
    
    # the list of the items
    def item_list(self):
        return self._prop_dict.keys()

    def item_ratings(self, item):
        return [int(pair[1]) for pair in self._prop_dict[item]]
    
    # prints same statistics
    def gen_report(self):
        print('Dataset: ', self._filename)
        print('Number of entries: ', self.tot_entries())
        print('Total number of users: ', self.tot_number_users())
        print('Total number of items: ', self.tot_number_items())
        print('Positive ratings average: ', self.posrate_average_item())
        print('Negative ratings average: ', self.negrate_average_item())
        print('Positive users\' average: ', self.positive_user_average())
        print('Negative users\' average: ', self.negative_user_average())
        print('sparsity: ', self.sparsity())
        print('(#positive_rating, #item_positively_rated):\n', self.posrate_frequency()) 
        #rat_freq, item_freq = reader.posrate_frequency()
        #print('Number of positive ratings: ', rat_freq)
        #print('Number of items rated positively: ', item_freq)
        #report_graph(rat_freq, item_freq, self._filename[:self._filename.index('.')])
        #print('Number of negative profile: ', self.avg_number_neg_profile())
        print('\n')
    
    def positive_user_average(self):
        pos_dict = co.defaultdict()

        for item in self._prop_dict:
            for user, rate in self._prop_dict[item]:
                if rate == '1':
                    if user in pos_dict:
                        pos_dict[user] = pos_dict[user] + 1
                    else:
                        pos_dict[user] = 1

        return sum([pos_dict[key] for key in pos_dict])/self.tot_number_users()

    def negative_user_average(self):
        neg_dict = co.defaultdict()

        for item in self._prop_dict:
            for user, rate in self._prop_dict[item]:
                if rate == '0':
                    if user in neg_dict:
                        neg_dict[user] = neg_dict[user] + 1
                    else:
                        neg_dict[user] = 1

        return sum([neg_dict[key] for key in neg_dict])/self.tot_number_users()


    def _items_posneg_rate(self, report):
        for x in self._prop_dict:
            report.write("Item {0} -> (pos_rate: {1}, neg_rate: {2})".format(x, self._posrate_item(x), self._negrate_item(x)) + "\n")

    def user_profile(self, user):
        user_profile = list()

        for item in self._prop_dict:
            for rate in self._prop_dict[item]:
                if rate[0] == user:
                    user_profile.append((item, rate[1]))

        return user_profile

    def _posrate_user(self, user_profile):
        return sum([1 for rate in user_profile if rate[1] == '1'])

    def _negrate_user(self, user_profile):
        return sum([1 for rate in user_profile if rate[1] == '0'])

    def sparsity(self):
        return 1 - (self.tot_entries())/(self.tot_number_items()*self.tot_number_users())

    def avg_number_neg_profile(self):
        sum = 0

        for user in self._user_list:
            profile = self.user_profile(user)
            sum += 1 if self._negrate_user(profile) > self._posrate_user(profile) else 0

        return sum/self.tot_number_users()

    def posrate_frequency(self):
        return co.Counter([self._posrate_item(item) for item in self._prop_dict])

def report_graph(rating_freq, item_freq, dataset):

    plt.ylabel('number of items rated')
    plt.xlabel('number of positive ratings')
    line, = plt.plot(numpy.array(rating_freq), numpy.array(item_freq), '--', linewidth=3)
    plt.savefig('rating_distr_{0}.pdf'.format(dataset), bbox_inches='tight', dpi='1000')
    plt.clf()

if __name__ == '__main__':
    #reader = DatasetAnalyzer("lastfm_bin.tsv", delimiter='\t')
    #reader.gen_report()
    reader = DatasetAnalyzer("train.csv")
    reader.gen_report()
    
    reader = DatasetAnalyzer("test.csv")
    reader.gen_report()

    '''
    reader = DatasetAnalyzer("eswc.tsv", delimiter='\t')
    reader.gen_report()

    reader = DatasetAnalyzer("movielens_bin.tsv", delimiter='\t')
    reader.gen_report()
    '''