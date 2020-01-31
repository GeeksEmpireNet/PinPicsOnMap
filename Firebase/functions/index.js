'use strict';

const admin = require('firebase-admin');
const functions = require('firebase-functions');
const nodemailer = require('nodemailer');
const util = require('util');
const fs = require('fs');
/*[Inner JS Class]*/
const sanitizer = require('./sanitizer');

admin.initializeApp();

const gmailEmail = "GeeksEmpire.Net@gmail.com";//Email Will Send As Default Alias
//GeeksEmpireInc@gmail.com
//GeeksEmpire.Net@gmail.com
const gmailPassword = "dgzkjtnwmhsxydeb";//AppPassword
//omdcpzvbpxdcmwyq
//dgzkjtnwmhsxydeb
const mailTransport = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

/*[Send New Promotion Email To User]*/
exports.sendPromotionEmail = functions.https.onCall((data, context) => {
  const UserUID = data.UserUID;
  const UserEmail = data.UserEmail;
  const Username = data.Username;
  const CurrentCountry = data.CurrentCountry;
  const CurrentCity = data.CurrentCity;
  const Latitude = data.Latitude;
  const Longitude = data.Longitude;
  const KnownName = data.KnownName;

  //Database
  var databaseReference = admin.database().ref()
  databaseReference.child(UserUID).child('WelcomeEmail').child('Sent').on("value", function (snapshot) {
    console.log(snapshot.val());

    if (snapshot.val()) {
      sendEmailTravelPromotions();
    } else {
      sendEmailWelcome(UserEmail, Username)
      databaseReference.child(UserUID).set({
        WelcomeEmail: {
          "Sent": true,
          "Email": UserEmail,
          "Username": Username
        }
      })
    }

  }, function (errorObject) {
    console.log("Read Failed: " + errorObject.code);

    sendEmailWelcome(UserEmail, Username)
  });

  return "Getting User Information"
});

async function sendEmailWelcome(email, displayName) {
  const mailOptions = {
    from: `Pin Pics On Map PinPicsOnMap@GeeksEmpire.net`,
    to: email,
  };

  //The user subscribed to the newsletter.
  mailOptions.subject = `Welcome To Pin Pics On Map! 🏞 `;
  //mailOptions.text = `Hey ${displayName || ''}! Welcome to ${APP_NAME}. I hope you will enjoy our service.`;
  var templateHtml = fs.readFileSync('PinPicsOnMap.html', "utf-8").toString();
  mailOptions.html = templateHtml
  await mailTransport.sendMail(mailOptions);

  console.log('Promotional Email Sent To :::', email);

  return "Email Sent";
}

async function sendEmailTravelPromotions() {


}

/*[Retrieve Public Internet Address]*/
exports.fetchUserPublicInternetAddress = functions.https.onRequest((req, res) => {
  var ipAddress;
  const ipAddressFastlyIP = req.headers['fastly-client-ip'];
  const ipAddressForwardX = req.headers['x-forwarded-for'];

  if (ipAddressFastlyIP != "undefined") {
    ipAddress = ipAddressFastlyIP;
  }
  if (ipAddressForwardX != "undefined") {
    ipAddress = ipAddressForwardX;
  }

  var callBackResult = {
    data: {
      "ClientAddressIP": ipAddress,
    },
  };

  res.send(callBackResult);
});

/*[Group Notification Functions]*/
const runtimeGroupMessageOptions = {
  timeoutSeconds: 313,
}
exports.groupMessageNotification = functions.runWith(runtimeGroupMessageOptions).https.onCall((data, context) => {

  const ChatName = data.ChatName;
  const topic = data.topic;
  const myUID = data.myUID;
  const ChatAction = data.ChatAction;
  const notificationLargeIcon = data.notificationLargeIcon
  const messageContent = data.messageContent;
  const CountryName = data.CountryName;
  const CityName = data.CityName;
  const defaultColor = data.defaultColor;
  const titleColor = data.titleColor;
  const contentColor = data.contentColor;

  console.log(" ➡➡➡ " + topic + " - " + messageContent + " - " + defaultColor)

  if (!(typeof messageContent === 'string') || messageContent.length === 0) {
    throw new functions.https.HttpsError('invalid-argument', 'The function must be called with ' +
      'one arguments "text" containing the message text to add.');
  }

  //create different approach for ananymous users for emergency
  if (!context.auth) {
    throw new functions.https.HttpsError('failed-precondition', 'The function must be called ' +
      'while authenticated.');
  }

  const uid = context.auth.uid;
  const name = context.auth.token.name || null;
  const picture = context.auth.token.picture || null;
  const email = context.auth.token.email || null;

  const sanitizedMessage = sanitizer.sanitizeText(messageContent);

  console.log("*** ", "Optionally send a push notification with the message.")

  var message = {

    android: {
      ttl: (3600 * 1000) * (3), // 3 hour in milliseconds

      priority: 'high',
    },

    data: {
      "ChatAction": ChatAction,
      "ChatName": ChatName,
      "myUID": myUID,
      "notificationLargeIcon": notificationLargeIcon,
      "notificationTitle": name,
      "notificationContent": sanitizedMessage,
      "defaultColor": defaultColor,
      "titleColor": titleColor,
      "contentColor": contentColor,
      "CountryName": CountryName,
      "CityName": CityName
    },

    topic: topic
  };

  return admin.messaging().send(message).then((response) => {
    console.log('Successfully sent message:', response);
  })
    .catch((error) => {
      console.log('Error sending message:', error);
    });

});

exports.groupOnEditNotification = functions.runWith(runtimeGroupMessageOptions).https.onCall((data, context) => {

  const ChatName = data.ChatName;
  const topic = data.topic;
  const myUID = data.myUID;
  const ChatAction = data.ChatAction;
  const notificationLargeIcon = data.notificationLargeIcon
  const messageContent = data.messageContent;
  const CountryName = data.CountryName;
  const CityName = data.CityName;
  const defaultColor = data.defaultColor;
  const titleColor = data.titleColor;
  const contentColor = data.contentColor;

  console.log(" ➡➡➡ " + topic + " - " + messageContent + " - " + defaultColor)

  if (!(typeof messageContent === 'string') || messageContent.length === 0) {
    throw new functions.https.HttpsError('invalid-argument', 'The function must be called with ' +
      'one arguments "text" containing the message text to add.');
  }

  //create different approach for ananymous users for emergency
  if (!context.auth) {
    throw new functions.https.HttpsError('failed-precondition', 'The function must be called ' +
      'while authenticated.');
  }

  const uid = context.auth.uid;
  const name = context.auth.token.name || null;
  const picture = context.auth.token.picture || null;
  const email = context.auth.token.email || null;

  const sanitizedMessage = sanitizer.sanitizeText(messageContent);

  console.log("*** ", "Optionally send a push notification with the message.")

  var message = {

    android: {
      ttl: (3600 * 1000) * (3), // 3 hour in milliseconds

      priority: 'high',
    },

    data: {
      "ChatAction": ChatAction,
      "ChatName": ChatName,
      "myUID": myUID,
      "notificationLargeIcon": notificationLargeIcon,
      "notificationTitle": name,
      "notificationContent": sanitizedMessage,
      "defaultColor": defaultColor,
      "titleColor": titleColor,
      "contentColor": contentColor,
      "CountryName": CountryName,
      "CityName": CityName
    },

    topic: topic
  };

  return admin.messaging().send(message).then((response) => {
    console.log('Successfully sent message:', response);
  })
    .catch((error) => {
      console.log('Error sending message:', error);
    });

});

/*[Group User States Functions]*/
exports.onlinehUserGroupChatStatus = functions.https.onCall((data, context) => {
  const CountryName = data.CountryName;
  const CityName = data.CityName;
  const ChatName = data.ChatName;
  const UID = data.UID;

  console.log('PinPicsOnMap/Messenger/' + CountryName + '/' + CityName + '/' + 'People/' + UID)

  const firestore = admin.firestore();
  try {
    const settings = { timestampsInSnapshots: true };
    firestore.settings(settings);
  } catch (exceptions) { }

  firestore.doc(
    'PinPicsOnMap/'
    + 'People/'
    + 'Profile/'
    + UID)
    .update({ 'userStates': "true" }).then(result => {

    }).catch(error => {
      console.log('⚠ ERROR ', error);
    })

  return firestore.doc(
    'PinPicsOnMap/'
    + 'Messenger/'
    + CountryName + '/'
    + CityName + '/'
    + 'People/'
    + UID)
    .update({ 'userStates': "true" }).then(result => {
      return true
    }).catch(error => {
      console.log('⚠ ERROR ', error);
      return false
    })
});

exports.offlinehUserGroupChatStatus = functions.https.onCall((data, context) => {
  const CountryName = data.CountryName;
  const CityName = data.CityName;
  const ChatName = data.ChatName;
  const UID = data.UID;

  console.log('PinPicsOnMap/Messenger/' + CountryName + '/' + CityName + '/' + 'People/' + UID)

  const firestore = admin.firestore();
  try {
    const settings = { timestampsInSnapshots: true };
    firestore.settings(settings);
  } catch (exceptions) { }

  firestore.doc(
    'PinPicsOnMap/'
    + 'People/'
    + 'Profile/'
    + UID)
    .update({ 'userStates': "false" }).then(result => {

    }).catch(error => {
      console.log('⚠ ERROR ', error);
    })

  return firestore.doc(
    'PinPicsOnMap/'
    + 'Messenger/'
    + CountryName + '/'
    + CityName + '/'
    + 'People/'
    + UID)
    .update({ 'userStates': "false" }).then(result => {
      return true
    }).catch(error => {
      console.log('⚠ ERROR ', error);
      return false
    })
});

/*[Group Messages SeenBy]*/
exports.updateGroupChatSeenBy = functions.https.onCall((data, context) => {
  const SeenByPath = data.SeenByPath;
  const MessagePath = data.MessagePath;

  const firestore = admin.firestore();
  try {
    const settings = { timestampsInSnapshots: true };
    firestore.settings(settings);
  } catch (exceptions) { }

  return firestore
    .collection(SeenByPath)
    .get().then(snapshot => {
      firestore.doc(MessagePath)
        .update({ 'SeenByNumber': snapshot.size.toString() }).then(result => {
          return true
        }).catch(error => {
          return false
        })
    })
    .catch(error => {
      console.log('⚠ ERROR ', error);
      return false
    });
});