import os

pid = os.popen("/home/jdk8/bin/jps |grep MainServer |awk '{print $1}'").read()
print "pid = ",pid


lines = os.popen("/home/jdk8/bin/jstat -gcoldcapacity %s"%pid).readlines()
oldName = []
oldValue = []

oldName = lines[0].split()
oldValue = lines[1].split()



# lines = os.popen("/home/jdk8/bin/jstat -gcnewcapacity %s"%pid).readlines()
# newName = []
# newValue = []
#
# newName = lines[0].split()
# newValue = lines[1].split()
#
#
# lines = os.popen("/home/jdk8/bin/jstat -gcmetacapacity %s"%pid).readlines()
# metaName = []
# metaValue = []
#
# metaName = lines[0].split()
# metaValue = lines[1].split()
#

lines = os.popen("/home/jdk8/bin/jstat -gccapacity %s"%pid).readlines()
allName = []
allValue = []

allName = lines[0].split()
allValue = lines[1].split()


# ([A-Z0-9]+:)( )([A-Z0-9 ]+)([A-Za-z ()]+)(.)
# print "$3 %s $4"%allValue[1]
# \s+$
def kb2mb(kb):
    return float(kb)/float(1024)

def bytes_conversion(number):
    symbols = ('K','M','G','T','P','E','Z','Y')
    prefix = dict()
    for i,s in enumerate(symbols):
        prefix[s] = 1<<(i+1) *10
    for s in reversed(symbols):
        if int(number) >= prefix[s]:
            value = float(number) / prefix[s]
            return '%.2f%s' %(value,s)
    return "%sB" %number

def kbytes_conversion(number):
    return bytes_conversion(number*1024)

print "-------------------------------------------------------"
print allName
print allValue
print "-------------------------------------------------------"
print "Minimum new generation capacity  %s"%kbytes_conversion(float(allValue[0]))
print "Maximum new generation capacity  %s"%kbytes_conversion(float(allValue[1]))
print "Current new generation capacity  %s"%kbytes_conversion(float(allValue[2]))
print "Current survivor space 0 capacity  %s"%kbytes_conversion(float(allValue[3]))
print "Current survivor space 1 capacity  %s"%kbytes_conversion(float(allValue[4]))
print "Current eden space capacity  %s"%kbytes_conversion(float(allValue[5]))
print "Minimum old generation capacity  %s"%kbytes_conversion(float(allValue[6]))
print "Maximum old generation capacity  %s"%kbytes_conversion(float(allValue[7]))
print "Current old generation capacity  %s"%kbytes_conversion(float(allValue[8]))
print "Current old space capacity  %s"%kbytes_conversion(float(allValue[9]))
print "Minimum metaspace capacity  %s"%kbytes_conversion(float(allValue[10]))
print "Maximum metaspace capacity  %s"%kbytes_conversion(float(allValue[11]))
print "Metaspace capacity  %s"%kbytes_conversion(float(allValue[12]))
print "Compressed class space minimum capacity  %s"%kbytes_conversion(float(allValue[13]))
print "Compressed class space maximum capacity  %s"%kbytes_conversion(float(allValue[4]))
print "Compressed class space capacity  %s"%kbytes_conversion(float(allValue[15]))
print "Number of young generation GC event %s "%allValue[16]
print "Number of full GC event %s "%allValue[17]

print dict(zip(allName,allValue))