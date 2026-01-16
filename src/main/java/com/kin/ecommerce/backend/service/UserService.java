package com.kin.ecommerce.backend.service;

import com.kin.ecommerce.backend.api.model.LoginBody;
import com.kin.ecommerce.backend.api.model.RegistrationBody;
import com.kin.ecommerce.backend.exception.EmailFailureException;
import com.kin.ecommerce.backend.exception.UserNotVerifiedException;
import com.kin.ecommerce.backend.model.VerificationToken;
import com.kin.ecommerce.backend.model.dao.LocalUserDAO;
import com.kin.ecommerce.backend.model.LocalUser;
import com.kin.ecommerce.backend.exception.UserAlreadyExistsException;
import com.kin.ecommerce.backend.model.dao.VerificationTokenDAO;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private LocalUserDAO localUserDAO;

    private VerificationTokenDAO verificationTokenDAO;

    private EncryptionService encryptionService;

    private JWTService jwtService;

    private EmailService emailService;

    public UserService(LocalUserDAO localUserDAO, Validator validator, VerificationTokenDAO verificationTokenDAO, EncryptionService encryptionService,
                       JWTService jwtService, EmailService emailService) {
        this.localUserDAO = localUserDAO;
        this.verificationTokenDAO = verificationTokenDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if ( localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent() ) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken = createVerificationToken(user);

        emailService.sendVerificationEmail(verificationToken);
        verificationTokenDAO.save(verificationToken);
        user = localUserDAO.save(user);
        return user;
    }

    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimeStamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws EmailFailureException, UserNotVerifiedException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername());

        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtService.generateJWT(user);
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.size() == 0
                            || verificationTokens.get(0).getCreatedTimeStamp().before(new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000));

                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);
        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getUser();
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                localUserDAO.save(user);
                verificationTokenDAO.deleteByUser(user);
                return true;
            }
        }
        return false;
    }
}
