# Vigilant Bans
JavaFX application that manages installation of the [RCVolus/lol-pick-ban-ui](https://github.com/RCVolus/lol-pick-ban-ui) 
project and allows some basic configuration.  

## Requirements
* Java 17 (e.g. from [Eclipse Adoptium](https://adoptium.net/temurin/releases/?version=17))
* Windows 10 or Windows 11 (3rd party requirement as the installed tool depends on the local League of Legends client.)

## Set-Up / Usage
After downloading the archive (zip or tar), the application can be started using the ``bin/vigilant-bands.bat`` file.  
When run, the application will use ``<working-directory>/cache`` to store and look up its dependencies.  
If the required dependencies are missing, the installer window will open. A single click on "Install" will install those.  

When the installation has finished, the application can be closed and re-opened. The small manager UI will show now:
* Start/Stop controls for both the LCU-Broker and the EU-Layout Server. (Remember __to start the League of Legends client!__)  
* The configuration feature allows the user to update the ``config.json`` in real time.  
  _Note: The logo in the middle of the overlay can only be changed when the layout is offline._
* The overlay will come available at [http://localhost:3000/?backend=ws://localhost:8999](http://localhost:3000/?backend=ws://localhost:8999)

## Disclaimer(s) and licenses
__This is a standalone project from FearNixx. Riot Games does not endorse or sponsor this project.__  
_The project has not been certified as ToS-compliant yet. It is still in early development._  
__In addition,__ FearNixx is not affiliated or partnered with Riot community volunteers.

The license [included in this repository](./LICENSE) only applies to this application in distributed form and its source code.    
When running, this application installs 3rd party software from their original distribution locations which have their own licenses:
* [NodeJS](https://nodejs.org) with a [proprietary license](https://github.com/nodejs/node/blob/master/LICENSE)
* [lol-pick-ban-ui](https://github.com/RCVolus/lol-pick-ban-ui) licensed under [MIT](https://github.com/RCVolus/lol-pick-ban-ui/blob/master/LICENSE)
  * Assets downloaded from Riot are licensed under the ["Legal Jibber Jabber"](https://www.riotgames.com/en/legal)
  
_Your usage of this application must comply to all the licenses listed above_.