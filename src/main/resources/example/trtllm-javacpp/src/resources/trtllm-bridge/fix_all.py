import os
import glob
def fix_all(dir):
    for root, _, files in os.walk(dir):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                with open(path, 'r', encoding='utf-8') as f_in:
                    lines = f_in.readlines()
                lines_to_delete = set()
                for i, line in enumerate(lines):
                    if 'std::' in line:
                        lines_to_delete.add(i)
                        # check backwards
                        j = i - 1
                        while j >= 0:
                            if not lines[j].strip():
                                j -= 1
                                continue
                            if lines[j].strip().endswith(';') or lines[j].strip().endswith('}') or lines[j].strip().endswith('{'):
                                break
                            lines_to_delete.add(j)
                            if 'public ' in lines[j] or 'privimport os
import glob
deotimport gindef fix_al
     for root, _,          for f in files:
            if              if f.endsw                  path = os.path.joi                  with open(path, 'r', encodi                      lines = f_in.readlines()
                ls_                lines_to_delete = set()
                     for i, line in enumerain                    if 'std::' in line:
                                 lines_to_delet                          # check backwards
   te                        j = i - 1
      .j                        while j ne                            if not les                                j -= 1
            ',                                conti                              if lines[j]                                  break
                            lines_to_delete.add(j)
                          '
"
