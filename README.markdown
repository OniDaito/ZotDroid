ZotDroid
========

A Zotero Client for Android devices
-----------------------------------

[Zotero](http://www.zotero.org) is a fantasic program for anyone doing any kind of research. Seriously! I'm a research student in Computer Science and Bioinformatics and I could not do what I do without Zotero. Accept no substitutes! :)


Current Version
---------------

This is the beta version I was working on until I couldn't anymore. It has most of the features in place but there are some things that still need adding:

* Upload to personal webdav needs testing.
* Search needs a lot of improvement.
* Performance and UX needs improvement.


Building and Testing
--------------------

This is an AndroidStudio project and should just drop right in. However, you do need to download some existing libraries first, listed in the next section.

If you follow the instructions on that page, you should get a set of jars that you can place in the lib directory within this project. Make sure they are added as dependencies and it should all compile fine.

### External Libraries in Use

*[Sardine](https://github.com/lookfirst/sardine) - WebDav Upload - [Usage Guide](https://github.com/lookfirst/sardine/wiki/UsageGuide).
*[Signpost](https://github.com/mttkay/signpost) - OAUTH handling.
* me.maxwin.view - for the scrolling and refresh.

Things that are done
--------------------

* Pagination
* Reading a fresh copy of a Zotero Library
* Downloading an attachment via the WebDav interface
* Basic search on all fields
* Selecting a collection and viewing the items for just that collection
* Incremental syncing
* Backing up via SQLite DB
* Support for Zotero cloud storage
* Storing the database on an SDCard
* Option as to where to save attachments
* Tag support
* Modifying subNotes
* Icons to show if an attachment is downloaded already (partial)
* Multiple Author items
* Sorting via multiple options such as date
* Group collections
* Zotero WebDav upload

Acknowledgements
----------------

* The [Zotero](https://www.zotero.org) crew for making a wicked awesome program!
* ZotDroid makes use of [Signpost](https://github.com/mttkay/signpost). This lives in the external directory for these who wish to build ZotDroid themselves.
* The [Zandy](https://github.com/avram/zandy) project, for an idea on how OAUTH works.
* [XListView-Android](https://github.com/Maxwin-z/XListView-Android) - For the very handy list dragging animations and event handling.

Licence
-------

    ZotDroid - An Android client for Zotero
    Copyright (C) 2017  Benjamin Blundell

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.


