# K-Input-Driver | input reader (more of reader)

## KIND WARNING
***THE DRIVER DOES NOT START ON DEVICE ATTACH OR REMOVE JUST HAVEN'T CONFIGURED IT YET THUS WILL EXIT APPLICATION ON DEVICE REMOVE BUT NOT RESTART ON DEVICE INSERT***
> Will do so once I do code cleanup and optimizations.

The library contains simple input-driver that should be able
to read from any device with a defined name..

## Requires

1. libinput
    1. libinput list-devices
   > Looking for an alternative option to just parse the devices using the driver
2. evtest
   > Use this to find the device you want to support
3. xdotool # not mandatory
   > Used to execute key events but you can configure them as you'd please

## Setup

1. Create config file
   <file_path_and_name>(.(extension))? ```Just aslong as it contains text in it```
   ```
   identifier: HUION.*Pad # Prefer using regular expressions for ease of use but using full name would be better
        listen: buttons # what do you want to listen to 
            button: 254 # on button up event
                command: xdotool key q # if you 
            button_up: 256 
                command: xdtool key u
            button_down: 257
                command: echo 257
            key_down: 258
                command: echo "down 258"
            key_up: 259
                command: echo "up 259"
   ```
2. Run The application
    1. k-input-driver.kexe ```used default file path ~/config/k-input-driver.conf```
       > Will fail to run if the file doesn't exist
    2. k-input-driver.kexe -c <path to your config file>

## Quirks

1. On device remove the driver will stop
   > Solution
   > > Need to add insmod I think to listen for device attach/detach

2. Radial Devices not yet supported
   > Solution
   > > Contribute
   > > Currently not sure how to implement circular motions
3. Multiple device support
   > Solution
   > > Contribute 