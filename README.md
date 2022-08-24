# Oasis Check System

Oasis Check System provides a way to access customer information easily, securely, and quickly so that cashiers can make better decisions when cashing checks.

## Table of contents
* [Preview](#preview)
* [App Layout](#app-layout)
* [Features](#features)
* [Purpose](#purpose)
* [Tools](#tools)
* [Download](#download)

## Preview

<img src="Screenshots/OCS_Gif.gif" width="200" height="420"/>

## App Layout

Home Page | Sample Customer | Settings Page | NFC Tap
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:
<img src="Screenshots/Regular%20Device/Screenshot_20220127-210113_oneplus-oneplus8pro-portrait.png" width="355" /> | <img src="Screenshots/Regular%20Device/Screenshot_20220127-210521_oneplus-oneplus8pro-portrait.png" width="335" /> | <img src="Screenshots/Regular%20Device/Screenshot_20220127-210543_oneplus-oneplus8pro-portrait.png" width="340" /> | <img src="Screenshots/Regular%20Device/Screenshot_20220127-210525_oneplus-oneplus8pro-portrait.png" width="360" />

## Features

I made a lot of optimizations to ensure this app works offline, ensuring check cashing remains operational in case of connection issues.

* Home Page: 
  * View the total customers in the system
  * Dashboard to show number of added, verified (by NFC card), and viewed daily
* Sample Customer: 
  * View customer info like full name, year of birth, and phone number
  * Profile picture and ID picture can be viewed to ensure you are viewing correct customer
  * NFC card can be tapped to phone to save customer data to that card and given to customer for next visit
  * See history of all the times customer used their NFC Card
  * Profile picture, id picture, and phone number can be edited
* Settings Page: 
  * Account name can be viewed
  * NFC card data can be viewed or reset
  * Log out of account 
  * Ability to reset account quickly in case of any issues
* NFC Tap:
  * allow user info to be saved onto an NFC card
  * if NFC card is place onto device NFC reader, it will open customer information in the app

## Purpose
This app was created for Alkhanshali Inc and their stores to make check cashing easier, both for the cashier and the customer. Oasis Check System (OCS) get rids of hundreds of pages of ID's and digitizes all that information to make it easier and quicker to view and even update information. 

There is a lot of risk associated with check cashing and OCS hopes to ease the stress to save business time and money.
	
## Tools
* Android Studio
* Java
* Firebase (Storage and Authentication)
* NFC

## Download

Download [app!](https://play.google.com/store/apps/details?id=com.akapps.check_verification_system) (using an Android device)
