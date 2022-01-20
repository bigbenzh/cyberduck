import re


def replace(string,replace_dict):
    for key,value in replace_dict.items():
        string=string.replace(key,value)
    return string

pattern = re.compile("public static final License EMPTY_LICENSE[\s\S]+(?=};)};\n")
replace_dict = {"return false":"return true","return null":'return "1"','LocaleFactory.localizedString("Not a valid registration key", "License")':'"patched"','EMPTY_LICENSE':'PATCHED_LICENSE'}

f = open("core/src/main/java/ch/cyberduck/core/aquaticprime/LicenseFactory.java","r+",encoding="utf8")
licensefactory=f.read()

EMPTY_LICENSE = pattern.findall(licensefactory)[0]
PATCHED_LICENSE = replace(EMPTY_LICENSE,replace_dict)

#order sensitive
patched = licensefactory.replace(EMPTY_LICENSE,EMPTY_LICENSE+PATCHED_LICENSE).replace("LicenseFactory.EMPTY_LICENSE","LicenseFactory.PATCHED_LICENSE")
f.seek(0)
f.write(patched)
f.close()

replace_dict = {'this.setDefault("update.check", String.valueOf(true));':'this.setDefault("update.check", String.valueOf(false));'}
f = open("core/src/main/java/ch/cyberduck/core/preferences/Preferences.java","r+",encoding="utf8")
preferences=f.read()
patched=replace(preferences,replace_dict)

f.seek(0)
f.write(patched)
f.close()
