/*
 * OBIMP4J - Java OBIMP Lib
 * Copyright (C) 2013 alex_xpert
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.obimp.listener;

import com.obimp.cl.ContactListItem;

/**
 * ContactListListener
 * @author alex_xpert
 */
public interface ContactListListener {

    public void onAuthRequest(String userid, String reason);
    public void onAuthReply(String userid, boolean reply);
    public void onAuthRevoke(String userid, String reason);
    public void onLoadContactList(ContactListItem[] cl);
    
}
