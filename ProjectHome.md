**Auto Package** plugin performs live (background) packaging of web content files during development. It works for all web facets in a project. This plugin seems more functional then existing solution in IDEA.

Latest version: **1.35**

Plugin can be downloaded in a common way. Don't forget to check the [Jodd project](http://jodd.org) ;)


## Usage ##

1) Install Plugin.

2) Open some project with exploded Web artifacts.

3) Choose "File > Project Structure"

4) Choose some module that has Web facet.

5) Go to **Auto Package** tab and check the checkbox.

![http://idea-autopackage.googlecode.com/files/autopackage.png](http://idea-autopackage.googlecode.com/files/autopackage.png)

## IDEA 9 ##

Idea 9 has an alternative for autopackage that updates resources on frame deactivation or on shortcut. However, numerous people reported that such update is not so efficient as this one. So, you may want to turn this off if:

![http://idea-autopackage.googlecode.com/files/run.png](http://idea-autopackage.googlecode.com/files/run.png)

## Notes ##

This plugin works for one or more web content directories. It works in _exploded_ mode (which shoud be the prefered one for development). Please note that sometimes web browsers cache files etc., so please reload if changes are not visible. Check the statusbar for the information about the auto packaging.