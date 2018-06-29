copy_ant_files()
{
    PROJECTPATH=$1

    cp ./temp/gen_antbuild/build.xml $PROJECTPATH
    cp ./temp/gen_antbuild/local.properties $PROJECTPATH
    cp ./temp/gen_antbuild/project.properties $PROJECTPATH
    cp ./temp/gen_antbuild/proguard-project.txt $PROJECTPATH
    rm -rf ./temp
}

android create lib-project -t android-20 -k com.tvos.temp -p ./temp/gen_antbuild
copy_ant_files "./"

