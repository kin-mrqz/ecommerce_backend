package com.kin.ecommerce.backend.service;

import com.kin.ecommerce.backend.model.LocalUser;
import com.kin.ecommerce.backend.model.WebOrder;
import com.kin.ecommerce.backend.model.dao.WebOrderDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private WebOrderDAO webOrderDAO;

    public OrderService(WebOrderDAO webOrderDAO) {
        this.webOrderDAO = webOrderDAO;
    }

    public List<WebOrder> getOrders(LocalUser user) {
        return webOrderDAO.findByUser(user);
    }
}
