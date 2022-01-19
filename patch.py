import re

pattern = re.compile("public static final License EMPTY_LICENSE[\s\S]+(?=};)};\n")
replace_dict = {"return false":"return true","return null":'return "1"','LocaleFactory.localizedString("Not a valid registration key", "License")':'"patched"','EMPTY_LICENSE':'PATCHED_LICENSE'}

f = open("core/src/main/java/ch/cyberduck/core/aquaticprime/LicenseFactory.java","r+",encoding="utf8")
licensefactory=f.read()

EMPTY_LICENSE = pattern.findall(licensefactory)[0]

PATCHED_LICENSE = EMPTY_LICENSE
for key,value in replace_dict.items():
    PATCHED_LICENSE=PATCHED_LICENSE.replace(key,value)

patched = licensefactory.replace(EMPTY_LICENSE,EMPTY_LICENSE+PATCHED_LICENSE).replace("LicenseFactory.EMPTY_LICENSE","LicenseFactory.PATCHED_LICENSE")
f.seek(0)
f.write(patched)
f.close()
