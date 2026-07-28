package com.matrimonial.util;

import org.springframework.stereotype.Component;

/**
 * UTIL: EmailTemplateUtil
 *
 * Provides ready-made HTML email templates for all email types.
 * Keeps email formatting logic out of the service classes.
 *
 * Layer: Util (helper class, used by EmailService)
 */
@Component
public class EmailTemplateUtil {

    /**
     * HTML template for Account Registration OTP email.
     *
     * @param otp the 6-digit OTP code to embed in the email
     * @return formatted HTML string
     */
    public String buildRegistrationEmail(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #ea580c;">Prajapati Samaj Matrimonial</h2>
                    <p>Welcome!</p>
                    <p>Thank you for registering with Prajapati Samaj Matrimonial.</p>
                    <p>Your verification OTP code is:</p>
                    <div style="font-size: 32px; font-weight: bold; color: #ea580c; letter-spacing: 8px; margin: 20px 0;">
                      %s
                    </div>
                    <p style="color: #636e72;">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
                    <p>If you did not initiate this registration, please ignore this email.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * HTML template for the OTP / Forgot Password email.
     *
     * @param otp the 6-digit OTP code to embed in the email
     * @return formatted HTML string
     */
    public String buildForgotPasswordEmail(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #d63031;">Prajapati Samaj Matrimonial</h2>
                    <p>Hello,</p>
                    <p>We received a request to reset your password.</p>
                    <p>Your OTP code is:</p>
                    <div style="font-size: 32px; font-weight: bold; color: #d63031; letter-spacing: 8px; margin: 20px 0;">
                      %s
                    </div>
                    <p style="color: #636e72;">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
                    <p>If you did not request this, please ignore this email.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * HTML template for Account Deletion OTP email.
     *
     * @param otp the 6-digit OTP code to embed in the email
     * @return formatted HTML string
     */
    public String buildAccountDeletionEmail(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px; border-top: 4px solid #dc2626;">
                    <h2 style="color: #dc2626;">Prajapati Samaj Matrimonial</h2>
                    <p>Hello,</p>
                    <p>We received a request to permanently delete your Prajapati Samaj Matrimonial account.</p>
                    <p>Your verification OTP code for account deletion is:</p>
                    <div style="font-size: 32px; font-weight: bold; color: #dc2626; letter-spacing: 8px; margin: 20px 0;">
                      %s
                    </div>
                    <p style="color: #636e72;">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
                    <p style="color: #dc2626; font-weight: bold;">Warning: Verifying this code will permanently remove all your profile data and active memberships.</p>
                    <p>If you did not request account deletion, please secure your account immediately.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * HTML template sent when someone sends you a Like.
     *
     * @param likerName name of the person who liked
     * @return formatted HTML string
     */
    public String buildLikeNotificationEmail(String likerName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #d63031;">Prajapati Samaj Matrimonial</h2>
                    <p>Good news! <strong>%s</strong> liked your profile. 💛</p>
                    <p>Log in to view their profile and send an interest request.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>
                    <p style="font-size: 11px; color: #888;">Don't want to receive these email notifications? You can manage your email preferences in Profile Settings.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(likerName);
    }

    /**
     * HTML template sent when someone sends you an Interest Request.
     *
     * @param senderName name of the person who sent interest
     * @return formatted HTML string
     */
    public String buildInterestNotificationEmail(String senderName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #d63031;">Prajapati Samaj Matrimonial</h2>
                    <p><strong>%s</strong> has sent you an interest request. 🌟</p>
                    <p>Log in to Accept or Decline this request.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>
                    <p style="font-size: 11px; color: #888;">Don't want to receive these email notifications? You can manage your email preferences in Profile Settings.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(senderName);
    }

    /**
     * HTML template sent when an interest is accepted (mutual match).
     *
     * @param acceptorName name of the person who accepted
     * @return formatted HTML string
     */
    public String buildInterestAcceptedEmail(String acceptorName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #d63031;">Prajapati Samaj Matrimonial</h2>
                    <p>Congratulations! 🎉 <strong>%s</strong> has accepted your interest request.</p>
                    <p>Log in to view your matches.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>
                    <p style="font-size: 11px; color: #888;">Don't want to receive these email notifications? You can manage your email preferences in Profile Settings.</p>
                    <br/>
                    <p>Regards,<br/>Prajapati Samaj Team</p>
                  </div>
                </body>
                </html>
                """.formatted(acceptorName);
    }
}

