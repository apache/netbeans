import React from 'react';

export default ({stocked, name, price}) => {
    var name = stocked ? name : <span style={{color: 'red'}}> {name} </span>;
    return (
        <tr>
            <td>{name}</td>
            <td>{price}</td>
        </tr>
    );
};
