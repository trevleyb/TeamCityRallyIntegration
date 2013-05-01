Rally = {};

Rally.SettingsForm = OO.extend(BS.AbstractPasswordForm, {
    setupEventHandlers : function() {
        var that = this;
        Event.observe('testConnection', 'click', this.testConnection.bindAsEventListener(this), false);

        this.setUpdateStateHandlers({
            updateState: function() {
                that.storeInSession();
            },
            saveState: function() {
                that.submitSettings();
            }
        });
    },

    storeInSession : function() {
        $("submitSettings").value = 'storeInSession';
        BS.PasswordFormSaver.save(this, this.formElement().action, BS.StoreInSessionListener);
    },

    submitSettings : function() {
        $("submitSettings").value = 'store';
        this.removeUpdateStateHandlers();

        BS.PasswordFormSaver.save(this, this.formElement().action,OO.extend(BS.ErrorsAwareListener, this.createErrorListener()));
        return false;
    },

    createErrorListener: function() {
        var that = this;
        return {
            onEmptyUrlError : function(elem) {
                $("errorUrl").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("url"));
            },

            onInvalidUrlError : function(elem) {
                this.onEmptyUrlError(elem);
            },

            onEmptyUserNameError : function(elem) {
                $("errorUserName").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("userName"));
            },

            onEmptyPasswordError : function(elem) {
                $("errorPassword").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("password"));
            },

            onEmptyProxyUriError : function(elem) {
                this.onInvalidProxyUriError(elem);
            },

            onInvalidProxyUriError : function(elem) {
                $("errorProxyUri").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("proxyUri"));
            },

            onInvalidConnectionError : function(elem) {
                $("errorInvalidCredentials").innerHTML = elem.firstChild.nodeValue;
                that.highlightErrorField($("password"));
            },

            onCompleteSave : function(form, responseXML, err) {
                BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
                if (!err) {
                    BS.XMLResponse.processRedirect(responseXML);
                } else {
                    that.setupEventHandlers();
                }
            }
        }
    },

    testConnection: function () {
        $("submitSettings").value = 'testConnection';
        var listener = OO.extend(BS.ErrorsAwareListener, this.createErrorListener());
        var oldOnCompleteSave = listener['onCompleteSave'];
        listener.onCompleteSave = function(form, responseXML, err) {
            oldOnCompleteSave(form, responseXML, err);
            if (!err) {
                form.enable();
                var res = responseXML.getElementsByTagName("testConnectionResult");
                if (res.length > 0) { // trouble
                    BS.TestConnectionDialog.show(false, res[0].firstChild.nodeValue, $('testConnection'), 'container');
                }
                else {
                    BS.TestConnectionDialog.show(true, "", $('testConnection'), 'container');
                }
            }
        }
        BS.PasswordFormSaver.save(this, this.formElement().action, listener);
    },

    changeStatusProxy: function() {
        var status = $("proxyUsed").checked;
        $("proxyUri").disabled = !status;
        $("proxyUsername").disabled = !status;
        $("proxyPassword").disabled = !status;
    }
});
