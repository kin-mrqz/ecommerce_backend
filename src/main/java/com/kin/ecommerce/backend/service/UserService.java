package com.kin.ecommerce.backend.service;

import com.kin.ecommerce.backend.api.model.LoginBody;
import com.kin.ecommerce.backend.api.model.RegistrationBody;
import com.kin.ecommerce.backend.model.dao.LocalUserDAO;
import com.kin.ecommerce.backend.model.LocalUser;
import com.kin.ecommerce.backend.exception.UserAlreadyExistsException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private LocalUserDAO localUserDAO;

    private EncryptionService encryptionService;

    private JWTService jwtService;

    public UserService(LocalUserDAO localUserDAO, Validator validator, EncryptionService encryptionService, JWTService jwtService) {
        this.localUserDAO = localUserDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }
    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if ( localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent() ) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        //TODO: add encryption to password
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user = localUserDAO.save(user);
        return user;
    }

    public String loginUser(LoginBody loginBody) {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername());

        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }
}
