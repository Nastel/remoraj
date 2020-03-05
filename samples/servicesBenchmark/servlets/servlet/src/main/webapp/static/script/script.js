/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */
window.onload = function () {
// Get the modal
    let modal = document.getElementsByName ("details");

// Get the button that opens the modal
    let btn = document.getElementsByName ("openDetails");

// Get the <span> element that closes the modal
    let span = document.getElementsByName ("close");

// When the user clicks on the button, open the modal
    btn.forEach ((element, index) => element.onclick = function () {
        modal[index].style.display = "block";
    });

// When the user clicks on <span> (x), close the modal
    span.forEach ((element, index) => element.onclick = function () {
        modal[index].style.display = "none";
    });

// When the user clicks anywhere outside of the modal, close it
    window.onclick = function (event) {
        modal.forEach (function (element) {
            if (event.target == element) element.style.display = "none";
        })
    };

};